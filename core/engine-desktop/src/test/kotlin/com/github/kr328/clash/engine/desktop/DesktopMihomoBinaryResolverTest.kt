package com.github.kr328.clash.engine.desktop

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DesktopMihomoBinaryResolverTest {
  @Test
  fun resolvesPackagedMihomoBinary() {
    val root = Files.createTempDirectory("tabby-mihomo-resources")
    val binary = root.resolve("mihomo").resolve("mihomo")
    Files.createDirectories(binary.parent)
    Files.writeString(binary, "")

    assertEquals(binary, DesktopMihomoBinaryResolver(root).resolve())
  }

  @Test
  fun preparesExecutableInInstallDirectory() {
    val root = Files.createTempDirectory("tabby-mihomo-install-resources")
    val installDir = Files.createTempDirectory("tabby-mihomo-install")
    val binary = root.resolve("mihomo").resolve("mihomo")
    Files.createDirectories(binary.parent)
    Files.writeString(binary, "mihomo")

    val executable = DesktopMihomoBinaryResolver(root).prepareExecutable(installDir)

    assertEquals(installDir.resolve("mihomo"), executable)
    assertEquals("mihomo", Files.readString(installDir.resolve("mihomo")))
  }

  @Test
  fun returnsNullWhenResourcesDirIsMissing() {
    assertNull(DesktopMihomoBinaryResolver(null).resolve())
  }

  @Test
  fun packagedProcessPreparesBinaryWhenConfigExists() {
    val root = Files.createTempDirectory("tabby-mihomo-process-resources")
    val homeDir = Files.createTempDirectory("tabby-mihomo-home")
    val binaryInstallDir = Files.createTempDirectory("tabby-mihomo-bin")
    val binary = root.resolve("mihomo").resolve("mihomo")
    Files.createDirectories(binary.parent)
    Files.writeString(binary, "mihomo")
    Files.writeString(homeDir.resolve("config.yaml"), "mixed-port: 7890")

    val process =
      packagedDesktopMihomoProcessOrNull(
        endpoint = DesktopMihomoEndpoint("127.0.0.1:9090"),
        binaryResolver = DesktopMihomoBinaryResolver(root),
        homeDir = homeDir,
        binaryInstallDir = binaryInstallDir,
      )

    assertNotNull(process)
    assertEquals("mihomo", Files.readString(binaryInstallDir.resolve("mihomo")))
  }

  @Test
  fun readsComposeResourcesDirProperty() {
    val root = Files.createTempDirectory("tabby-compose-resources")
    val previous = System.getProperty(DesktopComposeResources.RESOURCES_DIR_PROPERTY)

    try {
      System.setProperty(DesktopComposeResources.RESOURCES_DIR_PROPERTY, root.toString())

      assertEquals(root, DesktopComposeResources.resourcesDir())
    } finally {
      if (previous == null) {
        System.clearProperty(DesktopComposeResources.RESOURCES_DIR_PROPERTY)
      } else {
        System.setProperty(DesktopComposeResources.RESOURCES_DIR_PROPERTY, previous)
      }
    }
  }
}
