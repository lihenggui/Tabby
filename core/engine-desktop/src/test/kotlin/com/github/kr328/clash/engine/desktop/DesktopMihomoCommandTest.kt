package com.github.kr328.clash.engine.desktop

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DesktopMihomoCommandTest {
  @Test
  fun commandLineUsesMihomoConfigFlags() {
    val command =
      DesktopMihomoCommand(
        binary = Paths.binary,
        homeDir = Paths.homeDir,
        configFile = Paths.config,
        externalController = "127.0.0.1:9090",
        secret = "secret",
      )

    assertEquals(
      listOf(
        Paths.binary.toString(),
        "-d",
        Paths.homeDir.toString(),
        "-f",
        Paths.config.toString(),
        "-ext-ctl",
        "127.0.0.1:9090",
        "-secret",
        "secret",
      ),
      command.commandLine(),
    )
  }

  @Test
  fun validateCreatesMissingHomeDirectory() {
    val root = Files.createTempDirectory("tabby-mihomo-command")
    val binary = root.resolve("mihomo")
    val config = root.resolve("config.yaml")
    val home = root.resolve("home")

    Files.writeString(binary, "")
    Files.writeString(config, "mixed-port: 7890")

    DesktopMihomoCommand(binary = binary, homeDir = home, configFile = config).validate()

    assertEquals(true, Files.isDirectory(home))
  }

  @Test
  fun validateRejectsMissingConfigFile() {
    val root = Files.createTempDirectory("tabby-mihomo-command-missing-config")
    val binary = root.resolve("mihomo")
    Files.writeString(binary, "")

    assertFailsWith<IllegalArgumentException> {
      DesktopMihomoCommand(
          binary = binary,
          homeDir = root.resolve("home"),
          configFile = root.resolve("config.yaml"),
        )
        .validate()
    }
  }

  private object Paths {
    private val root = Files.createTempDirectory("tabby-mihomo-command-static")
    val binary = root.resolve("mihomo")
    val homeDir = root.resolve("home")
    val config = root.resolve("config.yaml")
  }
}
