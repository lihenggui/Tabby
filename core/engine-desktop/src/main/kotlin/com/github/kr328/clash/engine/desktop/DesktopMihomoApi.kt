package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.LogMessage
import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.Traffic
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.network.createTabbyHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.prepareGet
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class DesktopMihomoApi(
  private val endpoint: DesktopMihomoEndpoint,
  private val httpClient: HttpClient = createTabbyHttpClient(),
  private val clock: () -> Long = System::currentTimeMillis,
) {
  fun observeLogs(level: LogMessage.Level? = null): Flow<LogMessage> = flow {
    httpClient
      .prepareGet(
        endpoint.url(
          "logs",
          queryParameters = level?.toMihomoLogLevel()?.let { listOf("level" to it) } ?: emptyList(),
        )
      ) {
        authorize()
        timeout {
          requestTimeoutMillis = null
          socketTimeoutMillis = null
        }
      }
      .execute { response ->
        response.requireSuccess()
        val channel = response.bodyAsChannel()

        while (true) {
          val line = channel.readLine() ?: break
          if (line.isBlank()) continue

          emit(streamJson.parseToJsonElement(line).jsonObject.toLogMessage(clock()))
        }
      }
  }

  fun observeTraffic(): Flow<Traffic> = flow {
    httpClient
      .prepareGet(endpoint.url("traffic")) {
        authorize()
        timeout {
          requestTimeoutMillis = null
          socketTimeoutMillis = null
        }
      }
      .execute { response ->
        response.requireSuccess()
        val channel = response.bodyAsChannel()

        while (true) {
          val line = channel.readLine() ?: break
          if (line.isBlank()) continue

          emit(streamJson.parseToJsonElement(line).jsonObject.toTotalTraffic())
        }
      }
  }

  suspend fun queryTraffic(): Traffic {
    return observeTraffic().first()
  }

  suspend fun queryMode(): TunnelState.Mode? {
    return queryConfigs()["mode"]?.jsonPrimitive?.contentOrNull?.toTunnelMode()
  }

  suspend fun patchMode(mode: TunnelState.Mode) {
    patchConfigs(JsonObject(mapOf("mode" to JsonPrimitive(mode.toMihomoValue()))))
  }

  suspend fun queryConfigs(): JsonObject {
    return getJson("configs")
  }

  suspend fun queryVersion(): JsonObject {
    return getJson("version")
  }

  suspend fun queryProxyGroupNames(excludeNotSelectable: Boolean): List<String> {
    val mode = queryMode() ?: TunnelState.Mode.Rule

    if (mode == TunnelState.Mode.Direct) {
      return emptyList()
    }

    return queryGroups()
      .filterNot { it.boolean("hidden") == true }
      .filterNot { mode != TunnelState.Mode.Global && it.string("name") == GLOBAL_PROXY_GROUP }
      .filterNot { excludeNotSelectable && it.proxyType() != Proxy.Type.Selector }
      .mapNotNull { it.string("name") }
  }

  suspend fun queryProxyGroup(name: String, sort: ProxySort): ProxyGroup {
    val group =
      try {
        getJson("proxies", name)
      } catch (exception: DesktopMihomoApiException) {
        if (exception.status == HttpStatusCode.NotFound) {
          return ProxyGroup(Proxy.Type.Unknown, emptyList(), "")
        }
        throw exception
      }

    val names = group.proxyNameList()
    val proxyMap = queryProxyMap()
    val proxies =
      names
        .map { proxyName ->
          proxyMap[proxyName]?.toProxy()
            ?: Proxy(
              name = proxyName,
              title = proxyName,
              subtitle = "",
              type = Proxy.Type.Unknown,
              delay = 0,
            )
        }
        .sortBy(sort)

    return ProxyGroup(group.proxyType(), proxies, group.string("now").orEmpty())
  }

  suspend fun patchSelector(group: String, proxy: String): Boolean {
    val response =
      httpClient.put(endpoint.url("proxies", group)) {
        authorize()
        contentType(ContentType.Application.Json)
        setBody(JsonObject(mapOf("name" to JsonPrimitive(proxy))))
      }

    return when {
      response.status.value in 200..299 -> true
      response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.NotFound ->
        false
      else -> {
        response.requireSuccess()
        true
      }
    }
  }

  suspend fun healthCheck(group: String) {
    getJson(
      "group",
      group,
      "delay",
      queryParameters = healthCheckQueryParameters(),
    )
  }

  suspend fun healthCheckProxy(proxy: String) {
    getJson(
      "proxies",
      proxy,
      "delay",
      queryParameters = healthCheckQueryParameters(),
    )
  }

  private suspend fun queryGroups(): List<JsonObject> {
    return getJson("group")["proxies"]?.jsonArray?.mapNotNull { it.jsonObjectOrNull() }
      ?: emptyList()
  }

  private suspend fun queryProxyMap(): Map<String, JsonObject> {
    return getJson("proxies")["proxies"]?.jsonObject?.mapValuesNotNull { it.jsonObjectOrNull() }
      ?: emptyMap()
  }

  private suspend fun getJson(
    vararg pathSegments: String,
    queryParameters: List<Pair<String, String>> = emptyList(),
  ): JsonObject {
    val response =
      httpClient.get(endpoint.url(*pathSegments, queryParameters = queryParameters)) { authorize() }

    response.requireSuccess()

    return response.body()
  }

  private suspend fun patchConfigs(payload: JsonObject) {
    val response =
      httpClient.patch(endpoint.url("configs")) {
        authorize()
        contentType(ContentType.Application.Json)
        setBody(payload)
      }

    response.requireSuccess()
  }

  private fun HttpRequestBuilder.authorize() {
    endpoint.secret
      ?.takeIf { it.isNotEmpty() }
      ?.let {
        header(HttpHeaders.Authorization, "Bearer $it")
      }
  }
}

data class DesktopMihomoEndpoint(
  val controller: String,
  val secret: String? = null,
) {
  init {
    require(controller.isNotBlank()) { "mihomo controller address is blank" }
  }

  fun url(
    vararg pathSegments: String,
    queryParameters: List<Pair<String, String>> = emptyList(),
  ): String {
    val base =
      controller.trim().trimEnd('/').let {
        if (it.startsWith("http://") || it.startsWith("https://")) it else "http://$it"
      }

    return URLBuilder(base)
      .apply {
        this.pathSegments = pathSegments.filter { it.isNotBlank() }
        queryParameters.forEach { (name, value) -> parameters.append(name, value) }
      }
      .buildString()
  }
}

class DesktopMihomoApiException(val status: HttpStatusCode) :
  IllegalStateException("Mihomo external-controller request failed: HTTP ${status.value}")

private suspend fun HttpResponse.requireSuccess() {
  if (status.value !in 200..299) {
    throw DesktopMihomoApiException(status)
  }
}

private fun String.toTunnelMode(): TunnelState.Mode? {
  return when (lowercase()) {
    "direct" -> TunnelState.Mode.Direct
    "global" -> TunnelState.Mode.Global
    "rule" -> TunnelState.Mode.Rule
    else -> null
  }
}

private fun TunnelState.Mode.toMihomoValue(): String {
  return when (this) {
    TunnelState.Mode.Direct -> "direct"
    TunnelState.Mode.Global -> "global"
    TunnelState.Mode.Rule -> "rule"
  }
}

private const val GLOBAL_PROXY_GROUP = "GLOBAL"
private const val DEFAULT_HEALTH_CHECK_URL = "https://www.gstatic.com/generate_204"
private const val DEFAULT_HEALTH_CHECK_TIMEOUT_MILLIS = 5_000
private const val DEFAULT_HEALTH_CHECK_EXPECTED_STATUS = "204"

private fun healthCheckQueryParameters(): List<Pair<String, String>> {
  return listOf(
    "url" to DEFAULT_HEALTH_CHECK_URL,
    "timeout" to DEFAULT_HEALTH_CHECK_TIMEOUT_MILLIS.toString(),
    "expected" to DEFAULT_HEALTH_CHECK_EXPECTED_STATUS,
  )
}

private fun JsonObject.proxyNameList(): List<String> {
  return this["all"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
}

private fun JsonObject.toProxy(): Proxy {
  val name = string("name").orEmpty()
  val type = proxyType()

  return Proxy(
    name = name,
    title = name,
    subtitle = string("type").orEmpty(),
    type = type,
    delay = lastDelay(),
  )
}

private fun JsonObject.proxyType(): Proxy.Type {
  return string("type")?.toProxyType() ?: Proxy.Type.Unknown
}

private fun String.toProxyType(): Proxy.Type {
  return when (normalizeProxyType()) {
    "direct" -> Proxy.Type.Direct
    "reject" -> Proxy.Type.Reject
    "rejectdrop" -> Proxy.Type.RejectDrop
    "compatible" -> Proxy.Type.Compatible
    "pass" -> Proxy.Type.Pass
    "shadowsocks",
    "ss" -> Proxy.Type.Shadowsocks
    "shadowsocksr",
    "ssr" -> Proxy.Type.ShadowsocksR
    "snell" -> Proxy.Type.Snell
    "socks5" -> Proxy.Type.Socks5
    "http" -> Proxy.Type.Http
    "vmess" -> Proxy.Type.Vmess
    "vless" -> Proxy.Type.Vless
    "trojan" -> Proxy.Type.Trojan
    "hysteria" -> Proxy.Type.Hysteria
    "hysteria2" -> Proxy.Type.Hysteria2
    "tuic" -> Proxy.Type.Tuic
    "wireguard" -> Proxy.Type.WireGuard
    "dns" -> Proxy.Type.Dns
    "ssh" -> Proxy.Type.Ssh
    "mieru" -> Proxy.Type.Mieru
    "anytls" -> Proxy.Type.AnyTLS
    "sudoku" -> Proxy.Type.Sudoku
    "masque" -> Proxy.Type.Masque
    "trusttunnel" -> Proxy.Type.TrustTunnel
    "openvpn" -> Proxy.Type.OpenVPN
    "tailscale" -> Proxy.Type.Tailscale
    "gostrelay" -> Proxy.Type.GostRelay
    "relay" -> Proxy.Type.Relay
    "selector",
    "select" -> Proxy.Type.Selector
    "fallback" -> Proxy.Type.Fallback
    "urltest" -> Proxy.Type.URLTest
    "loadbalance" -> Proxy.Type.LoadBalance
    else -> Proxy.Type.Unknown
  }
}

private fun String.normalizeProxyType(): String {
  return lowercase().filterNot { it == '-' || it == '_' || it == ' ' }
}

private fun JsonObject.lastDelay(): Int {
  return this["history"]
    ?.jsonArray
    ?.lastOrNull()
    ?.jsonObjectOrNull()
    ?.get("delay")
    ?.jsonPrimitive
    ?.intOrNull ?: 0
}

private fun List<Proxy>.sortBy(sort: ProxySort): List<Proxy> {
  return when (sort) {
    ProxySort.Default -> this
    ProxySort.Title -> sortedBy { it.title }
    ProxySort.Delay -> sortedBy { it.delay }
  }
}

private fun JsonObject.toTotalTraffic(): Traffic {
  return Traffic.fromBytes(long("upTotal"), long("downTotal"))
}

private fun JsonObject.toLogMessage(time: Long): LogMessage {
  return LogMessage(
    level = string("type")?.toLogLevel() ?: LogMessage.Level.Unknown,
    message = string("payload").orEmpty(),
    time = time,
  )
}

private fun String.toLogLevel(): LogMessage.Level {
  return when (lowercase()) {
    "debug" -> LogMessage.Level.Debug
    "info" -> LogMessage.Level.Info
    "warning",
    "warn" -> LogMessage.Level.Warning
    "error" -> LogMessage.Level.Error
    "silent" -> LogMessage.Level.Silent
    else -> LogMessage.Level.Unknown
  }
}

private fun LogMessage.Level.toMihomoLogLevel(): String? {
  return when (this) {
    LogMessage.Level.Debug -> "debug"
    LogMessage.Level.Info -> "info"
    LogMessage.Level.Warning -> "warning"
    LogMessage.Level.Error -> "error"
    LogMessage.Level.Silent -> "silent"
    LogMessage.Level.Unknown -> null
  }
}

private fun JsonObject.long(name: String): Long {
  return this[name]?.jsonPrimitive?.longOrNull ?: 0
}

private val streamJson = Json {
  ignoreUnknownKeys = true
  explicitNulls = false
}

private fun JsonObject.string(name: String): String? {
  return this[name]?.jsonPrimitive?.contentOrNull
}

private fun JsonObject.boolean(name: String): Boolean? {
  return this[name]?.jsonPrimitive?.booleanOrNull
}

private inline fun <T> Map<String, JsonElement>.mapValuesNotNull(
  transform: (JsonElement) -> T?
): Map<String, T> {
  return mapNotNull { (name, element) -> transform(element)?.let { name to it } }.toMap()
}

private fun JsonElement.jsonObjectOrNull(): JsonObject? {
  return this as? JsonObject
}
