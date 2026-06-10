package com.github.kr328.clash.engine.desktop

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings

internal fun syncMihomoProviderCaches(
  profileDir: Path,
  mihomoHomeDir: Path,
  configFile: Path,
) {
  val providersDir = profileDir.resolve(PROVIDERS_DIR)
  if (providersDir.notExists()) return

  resolveMihomoProviderCaches(configFile, mihomoHomeDir).forEach { cache ->
    val source = providersDir.resolve(cache.relativePath)
    if (!source.isRegularFile()) return@forEach

    cache.target.parent?.createDirectories()
    Files.copy(source, cache.target, StandardCopyOption.REPLACE_EXISTING)
  }
}

private fun resolveMihomoProviderCaches(
  configFile: Path,
  mihomoHomeDir: Path,
): List<MihomoProviderCache> {
  val root = loadYamlMap(configFile) ?: return emptyList()

  return providerCaches(root["proxy-providers"], "proxies", mihomoHomeDir) +
    providerCaches(root["rule-providers"], "rules", mihomoHomeDir)
}

private fun providerCaches(
  providers: Any?,
  defaultPrefix: String,
  mihomoHomeDir: Path,
): List<MihomoProviderCache> {
  val mappings = providers as? Map<*, *> ?: return emptyList()

  return mappings.values.mapNotNull { provider ->
    val mapping = provider as? Map<*, *> ?: return@mapNotNull null
    val explicitPath = mapping["path"].asNonBlankString()

    if (explicitPath != null) {
      providerCacheForExplicitPath(explicitPath, mihomoHomeDir)
    } else {
      val url = mapping["url"].asNonBlankString() ?: return@mapNotNull null
      val relativePath = Path.of(defaultPrefix, md5Hex(url))
      MihomoProviderCache(relativePath, mihomoHomeDir.resolve(relativePath))
    }
  }
}

private fun providerCacheForExplicitPath(
  path: String,
  mihomoHomeDir: Path,
): MihomoProviderCache? {
  val homeDir = mihomoHomeDir.toAbsolutePath().normalize()
  val target =
    if (Path.of(path).isAbsolute) {
      Path.of(path).normalize()
    } else {
      homeDir.resolve(path).normalize()
    }
  if (!target.startsWith(homeDir)) return null

  val relativePath = homeDir.relativize(target)
  return MihomoProviderCache(relativePath, target)
}

private fun loadYamlMap(configFile: Path): Map<*, *>? {
  val settings = LoadSettings.builder().build()
  val parsed = Load(settings).loadFromString(configFile.toFile().readText())

  return parsed as? Map<*, *>
}

private fun Any?.asNonBlankString(): String? {
  return (this as? String)?.takeIf { it.isNotBlank() }
}

private fun md5Hex(value: String): String {
  val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(StandardCharsets.UTF_8))
  val chars = CharArray(digest.size * 2)

  digest.forEachIndexed { index, byte ->
    val unsigned = byte.toInt() and 0xff
    chars[index * 2] = HEX_CHARS[unsigned ushr 4]
    chars[index * 2 + 1] = HEX_CHARS[unsigned and 0x0f]
  }

  return String(chars)
}

private data class MihomoProviderCache(
  val relativePath: Path,
  val target: Path,
)

private val HEX_CHARS = "0123456789abcdef".toCharArray()
