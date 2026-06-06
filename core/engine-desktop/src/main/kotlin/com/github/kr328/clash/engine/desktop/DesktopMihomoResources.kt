package com.github.kr328.clash.engine.desktop

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path

class DesktopMihomoBinaryResolver(
  private val resourcesDir: Path? = DesktopComposeResources.resourcesDir()
) {
  fun resolve(): Path? {
    val root = resourcesDir ?: return null

    return BINARY_CANDIDATES.map(root::resolve).firstOrNull(Files::isRegularFile)
  }

  fun prepareExecutable(installDir: Path): Path? {
    val packaged = resolve() ?: return null
    val target = installDir.resolve(packaged.fileName)

    Files.createDirectories(installDir)
    Files.copy(packaged, target, StandardCopyOption.REPLACE_EXISTING)

    if (!target.fileName.toString().endsWith(".exe")) {
      target.toFile().setExecutable(true, true)
    }

    return target
  }

  companion object {
    private val BINARY_CANDIDATES =
      listOf(
        Path("mihomo/mihomo"),
        Path("mihomo/mihomo.exe"),
        Path("mihomo"),
        Path("mihomo.exe"),
      )
  }
}

object DesktopComposeResources {
  const val RESOURCES_DIR_PROPERTY = "compose.application.resources.dir"

  fun resourcesDir(): Path? {
    return System.getProperty(RESOURCES_DIR_PROPERTY)?.takeIf { it.isNotBlank() }?.let { Path(it) }
  }
}

fun defaultMihomoHomeDir(): Path {
  return Path(System.getProperty("user.home"), ".config", "mihomo")
}

fun defaultMihomoBinaryInstallDir(): Path {
  return Path(System.getProperty("user.home"), ".cache", "tabby", "mihomo")
}

fun packagedDesktopMihomoProcessOrNull(
  endpoint: DesktopMihomoEndpoint,
  binaryResolver: DesktopMihomoBinaryResolver = DesktopMihomoBinaryResolver(),
  homeDir: Path = defaultMihomoHomeDir(),
  binaryInstallDir: Path = defaultMihomoBinaryInstallDir(),
): DesktopMihomoProcess? {
  val configFile = homeDir.resolve("config.yaml")

  if (!Files.isRegularFile(configFile)) return null

  val binary = binaryResolver.prepareExecutable(binaryInstallDir) ?: return null

  return DesktopMihomoProcess(
    DesktopMihomoCommand(
      binary = binary,
      homeDir = homeDir,
      configFile = configFile,
      externalController = endpoint.controller,
      secret = endpoint.secret,
    )
  )
}
