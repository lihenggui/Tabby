package com.github.kr328.clash.engine.desktop

import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun interface DesktopMihomoConfigValidator {
  suspend fun validate(homeDir: Path, configFile: Path)
}

class DesktopMihomoCliConfigValidator(
  private val binary: Path,
  private val timeout: Duration = 30.seconds,
) : DesktopMihomoConfigValidator {
  override suspend fun validate(homeDir: Path, configFile: Path) {
    withContext(Dispatchers.IO) {
      val process =
        ProcessBuilder(
            binary.toString(),
            "-d",
            homeDir.toString(),
            "-f",
            configFile.toString(),
            "-t",
          )
          .directory(homeDir.toFile())
          .redirectErrorStream(true)
          .start()

      if (!process.waitFor(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)) {
        process.destroyForcibly()
        process.waitFor()
        throw DesktopMihomoConfigValidationException("mihomo configuration test timed out")
      }

      val output = process.inputStream.bufferedReader().readText().trim()
      if (process.exitValue() != 0) {
        throw DesktopMihomoConfigValidationException(
          output.ifBlank { "mihomo configuration test failed" }
        )
      }
    }
  }
}

class DesktopMihomoConfigValidationException(message: String) : IllegalStateException(message)
