package com.github.kr328.clash.engine.desktop

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DesktopMihomoProcess(private val command: DesktopMihomoCommand) {
  private var process: Process? = null

  val isRunning: Boolean
    get() = process?.isAlive == true

  fun start() {
    check(!isRunning) { "mihomo process is already running" }

    command.validate()

    process =
      ProcessBuilder(command.commandLine())
        .directory(command.homeDir.toFile())
        .redirectErrorStream(true)
        .start()
  }

  fun stop(timeout: Duration = 5.seconds) {
    val current = process ?: return

    current.destroy()
    if (!current.waitFor(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)) {
      current.destroyForcibly()
      current.waitFor()
    }

    process = null
  }
}

data class DesktopMihomoCommand(
  val binary: Path,
  val homeDir: Path,
  val configFile: Path,
  val externalController: String? = null,
  val secret: String? = null,
) {
  fun commandLine(): List<String> = buildList {
    add(binary.toString())
    add("-d")
    add(homeDir.toString())
    add("-f")
    add(configFile.toString())
    externalController?.let {
      add("-ext-ctl")
      add(it)
    }
    secret?.let {
      add("-secret")
      add(it)
    }
  }

  fun validate() {
    require(Files.isRegularFile(binary)) { "mihomo binary not found: $binary" }
    require(Files.isRegularFile(configFile)) { "mihomo config file not found: $configFile" }

    if (homeDir.notExists()) {
      homeDir.createDirectories()
    }
  }
}
