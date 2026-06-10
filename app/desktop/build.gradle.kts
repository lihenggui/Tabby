import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.jetbrains.compose)
}

dependencies {
  implementation(project(":app:shared"))
  implementation(compose.desktop.currentOs)
  implementation(libs.kotlinx.coroutines.swing)
}

private data class DesktopMihomoAsset(
  val resourceDirectory: String,
  val archiveName: String,
  val sha256: String,
  val executableName: String = "mihomo",
) {
  fun encode(): String {
    return listOf(resourceDirectory, archiveName, sha256, executableName).joinToString("|")
  }
}

private val DESKTOP_MIHOMO_VERSION = "v1.19.27"

@DisableCachingByDefault(because = "Downloads official mihomo release assets for desktop packages.")
abstract class PrepareDesktopMihomoResourcesTask : DefaultTask() {
  @get:Input abstract val releaseVersion: Property<String>

  @get:Input abstract val assets: ListProperty<String>

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun prepare() {
    val resourcesRoot = outputDirectory.get().asFile.toPath()
    val version = releaseVersion.get()

    assets.get().map(::decodeAsset).forEach { asset ->
      val archive = temporaryDir.toPath().resolve(asset.archiveName)
      val target =
        resourcesRoot
          .resolve(asset.resourceDirectory)
          .resolve("mihomo")
          .resolve(asset.executableName)

      Files.createDirectories(target.parent)
      download(downloadUrl(version, asset), archive)
      verifySha256(archive, asset.sha256)
      extractMihomoExecutable(archive, target, asset.executableName)

      if (!asset.executableName.endsWith(".exe")) {
        target.toFile().setExecutable(true, true)
      }
    }
  }

  private fun download(url: String, target: Path) {
    URI(url).toURL().openStream().use { input ->
      Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  private fun downloadUrl(version: String, asset: DesktopMihomoAsset): String {
    return "https://github.com/MetaCubeX/mihomo/releases/download/$version/${asset.archiveName}"
  }

  private fun decodeAsset(value: String): DesktopMihomoAsset {
    val parts = value.split("|")
    require(parts.size == 4) { "Invalid mihomo asset spec: $value" }

    return DesktopMihomoAsset(
      resourceDirectory = parts[0],
      archiveName = parts[1],
      sha256 = parts[2],
      executableName = parts[3],
    )
  }

  private fun verifySha256(path: Path, expected: String) {
    val actual = sha256(path)

    require(actual == expected) {
      "Unexpected SHA-256 for ${path.fileName}: expected $expected, actual $actual"
    }
  }

  private fun sha256(path: Path): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

    Files.newInputStream(path).use { input ->
      while (true) {
        val read = input.read(buffer)
        if (read < 0) break

        digest.update(buffer, 0, read)
      }
    }

    return digest.digest().joinToString(separator = "") { "%02x".format(it.toInt() and 0xff) }
  }

  private fun extractMihomoExecutable(
    archive: Path,
    target: Path,
    executableName: String,
  ) {
    if (archive.fileName.toString().endsWith(".gz")) {
      GZIPInputStream(Files.newInputStream(archive)).use { input ->
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
      }
      return
    }

    ZipInputStream(Files.newInputStream(archive)).use { zip ->
      while (true) {
        val entry = zip.nextEntry ?: break
        if (!entry.isDirectory && entry.name.substringAfterLast('/').endsWith(".exe")) {
          Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING)
          return
        }
      }
    }

    error("Unable to find $executableName in ${archive.fileName}")
  }
}

private val desktopMihomoAssets =
  listOf(
    DesktopMihomoAsset(
      resourceDirectory = "macos-x64",
      archiveName = "mihomo-darwin-amd64-compatible-v1.19.27.gz",
      sha256 = "ddfafe6993e0adf97420d126d5ce7868113174630ccbf36d4a1bee2784085172",
    ),
    DesktopMihomoAsset(
      resourceDirectory = "macos-arm64",
      archiveName = "mihomo-darwin-arm64-v1.19.27.gz",
      sha256 = "3617c9d8a5a55aecfe1ebd0f55ff59f2706c8ad68fd65c6c4e5f7cf2b74263f1",
    ),
    DesktopMihomoAsset(
      resourceDirectory = "linux-x64",
      archiveName = "mihomo-linux-amd64-compatible-v1.19.27.gz",
      sha256 = "36850c946615f5c712946b62dbbbd06f6941d6d8a7543b315198bcb24ada3ea9",
    ),
    DesktopMihomoAsset(
      resourceDirectory = "linux-arm64",
      archiveName = "mihomo-linux-arm64-v1.19.27.gz",
      sha256 = "87db0c6660a9557a901b5750f997967e71d8c0af07ea1d1dd4d04c28da7f7e6f",
    ),
    DesktopMihomoAsset(
      resourceDirectory = "windows-x64",
      archiveName = "mihomo-windows-amd64-compatible-v1.19.27.zip",
      sha256 = "9cddc00240ed90d0bbd8333c1ff2b83152eb03c6bcddfbb5a17714a3272c1e88",
      executableName = "mihomo.exe",
    ),
    DesktopMihomoAsset(
      resourceDirectory = "windows-arm64",
      archiveName = "mihomo-windows-arm64-v1.19.27.zip",
      sha256 = "dcbfe6f81a72dfb6b8b549f1aec32eb47644e592ac7dffefc026b15e9c25213a",
      executableName = "mihomo.exe",
    ),
  )

private val desktopMihomoResourcesRoot =
  layout.buildDirectory.dir("generated/desktopMihomoResources")

private val prepareDesktopMihomoResources =
  tasks.register<PrepareDesktopMihomoResourcesTask>("prepareDesktopMihomoResources") {
    releaseVersion.set(DESKTOP_MIHOMO_VERSION)
    assets.set(desktopMihomoAssets.map(DesktopMihomoAsset::encode))
    outputDirectory.set(desktopMihomoResourcesRoot)
  }

compose.desktop {
  application {
    mainClass = "com.github.kr328.clash.app.desktop.MainKt"

    nativeDistributions {
      targetFormats(
        TargetFormat.Dmg,
        TargetFormat.Msi,
        TargetFormat.Exe,
        TargetFormat.Deb,
        TargetFormat.Rpm,
      )
      packageName = "Tabby"
      packageVersion = "3.1.1"
      appResourcesRootDir.set(
        prepareDesktopMihomoResources.map { desktopMihomoResourcesRoot.get() }
      )
    }
  }
}
