package com.github.kr328.clash.engine.desktop

import com.github.kr328.clash.core.model.FetchStatus
import com.github.kr328.clash.core.model.Profile
import com.github.kr328.clash.core.model.ProfileFieldValidationError
import com.github.kr328.clash.core.model.isHttpProfileSource
import com.github.kr328.clash.core.model.profileFieldValidationError
import com.github.kr328.clash.core.model.profileFieldValidationErrorMessage
import com.github.kr328.clash.core.model.profileShouldFetchConfiguration
import com.github.kr328.clash.database.DesktopDatabaseDriverFactory
import com.github.kr328.clash.database.ProfileDatabase
import com.github.kr328.clash.database.ProfileEntity
import com.github.kr328.clash.engine.api.ProfileRepository
import com.github.kr328.clash.network.ProfileFetchResult
import com.github.kr328.clash.network.ProfileNetworkClient
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DesktopProfileRepository(
  private val homeDir: Path = defaultMihomoHomeDir(),
  private val database: ProfileDatabase =
    ProfileDatabase(
      DesktopDatabaseDriverFactory(homeDir.resolve("profiles.db").toString()).createDriver()
    ),
  private val networkClient: ProfileNetworkClient = ProfileNetworkClient(),
  private val configValidator: DesktopMihomoConfigValidator? = null,
  private val clock: () -> Long = System::currentTimeMillis,
  private val uuidFactory: () -> Uuid = { Uuid.random() },
) : ProfileRepository {
  private val profilesDir = homeDir.resolve("profiles")
  private val importedDir = profilesDir.resolve("imported")
  private val pendingDir = profilesDir.resolve("pending")
  private val processingDir = profilesDir.resolve("processing")
  private val activeProfileFile = homeDir.resolve("active-profile")
  private val activeConfigFile = homeDir.resolve(CONFIGURATION_FILE)
  private val lock = Mutex()
  private val profiles = MutableStateFlow<List<Profile>>(emptyList())

  init {
    listOf(homeDir, importedDir, pendingDir).forEach(Path::createDirectories)
    refreshProfiles()
  }

  override fun observeProfiles(): Flow<List<Profile>> {
    return profiles.asStateFlow()
  }

  override suspend fun queryProfiles(): List<Profile> =
    withContext(Dispatchers.IO) { lock.withLock { loadProfiles() } }

  override suspend fun queryByUuid(uuid: Uuid): Profile? =
    withContext(Dispatchers.IO) { lock.withLock { resolveProfile(uuid) } }

  override suspend fun queryActive(): Profile? =
    withContext(Dispatchers.IO) {
      lock.withLock {
        readActiveProfileUuid()?.takeIf { database.importedExists(it) }?.let(::resolveProfile)
      }
    }

  override suspend fun create(type: Profile.Type, name: String, source: String): Uuid =
    withContext(Dispatchers.IO) {
      lock.withLock {
        if (type == Profile.Type.External) {
          unsupported()
        }

        val uuid = generateProfileUuid()
        val createdAt = clock()
        database.upsertPending(
          ProfileEntity(
            uuid = uuid,
            name = name,
            type = type,
            source = source,
            interval = 0,
            upload = 0,
            download = 0,
            total = 0,
            expire = 0,
            createdAt = createdAt,
          )
        )

        pendingDir.resolve(uuid.toString()).prepareNewProfileDirectory()
        refreshProfiles()

        uuid
      }
    }

  override suspend fun clone(uuid: Uuid): Uuid =
    withContext(Dispatchers.IO) {
      lock.withLock {
        val imported =
          database.queryImportedByUuid(uuid)
            ?: throw FileNotFoundException("profile $uuid not found")
        val newUuid = generateProfileUuid()
        val createdAt = clock()

        copyProfileDirectory(
          importedDir.resolve(uuid.toString()),
          pendingDir.resolve(newUuid.toString()),
        )
        database.upsertPending(
          imported.copy(
            uuid = newUuid,
            type = Profile.Type.File,
            createdAt = createdAt,
          )
        )
        refreshProfiles()

        newUuid
      }
    }

  override suspend fun patch(uuid: Uuid, name: String, source: String, interval: Long) {
    withContext(Dispatchers.IO) {
      lock.withLock {
        val pending = database.queryPendingByUuid(uuid)

        if (pending != null) {
          database.upsertPending(
            pending.copy(
              name = name,
              source = source,
              interval = interval,
              upload = 0,
              download = 0,
              total = 0,
              expire = 0,
            )
          )
        } else {
          val imported =
            database.queryImportedByUuid(uuid)
              ?: throw FileNotFoundException("profile $uuid not found")

          copyProfileDirectory(
            importedDir.resolve(uuid.toString()),
            pendingDir.resolve(uuid.toString()),
          )
          database.upsertPending(
            imported.copy(
              name = name,
              source = source,
              interval = interval,
              upload = 0,
              download = 0,
              total = 0,
              expire = 0,
            )
          )
        }

        refreshProfiles()
      }
    }
  }

  override suspend fun update(uuid: Uuid) {
    withContext(Dispatchers.IO) {
      lock.withLock {
        val imported = database.queryImportedByUuid(uuid) ?: return@withLock
        if (imported.type != Profile.Type.Url) return@withLock

        copyProfileDirectory(importedDir.resolve(uuid.toString()), processingDir)
        val fetchedProfile =
          fetchProfileConfiguration(
            source = imported.source,
            profileDir = processingDir,
            force = true,
          )
        validateProfileDirectory(processingDir)

        copyProfileDirectory(processingDir, importedDir.resolve(uuid.toString()))
        database.updateImported(
          imported.withSubscriptionUserInfo(fetchedProfile, imported.createdAt)
        )
        if (readActiveProfileUuid() == uuid) syncActiveProfile(uuid)
        refreshProfiles()
      }
    }
  }

  override suspend fun commit(uuid: Uuid, onStatus: ((FetchStatus) -> Unit)?) {
    withContext(Dispatchers.IO) {
      lock.withLock {
        val pending =
          database.queryPendingByUuid(uuid)
            ?: throw FileNotFoundException("profile $uuid not found")

        pending.enforceFieldValid()
        copyProfileDirectory(pendingDir.resolve(uuid.toString()), processingDir)
        val fetchedProfile =
          fetchProfileConfiguration(
            source = pending.source,
            profileDir = processingDir,
            force = pending.type != Profile.Type.File,
          )
        validateProfileDirectory(processingDir)

        copyProfileDirectory(processingDir, importedDir.resolve(uuid.toString()))
        val old = database.queryImportedByUuid(uuid)
        database.upsertImported(
          pending.withSubscriptionUserInfo(fetchedProfile, old?.createdAt ?: clock())
        )
        database.deletePending(uuid)
        pendingDir.resolve(uuid.toString()).deleteRecursively()

        if (readActiveProfileUuid() == uuid) syncActiveProfile(uuid)
        refreshProfiles()
      }
    }
  }

  override suspend fun release(uuid: Uuid) {
    withContext(Dispatchers.IO) {
      lock.withLock {
        database.deletePending(uuid)
        pendingDir.resolve(uuid.toString()).deleteRecursively()
        refreshProfiles()
      }
    }
  }

  override suspend fun delete(uuid: Uuid) {
    withContext(Dispatchers.IO) {
      lock.withLock {
        database.deleteImported(uuid)
        database.deletePending(uuid)
        importedDir.resolve(uuid.toString()).deleteRecursively()
        pendingDir.resolve(uuid.toString()).deleteRecursively()

        if (readActiveProfileUuid() == uuid) {
          deleteActiveProfile()
        }

        refreshProfiles()
      }
    }
  }

  override suspend fun setActive(profile: Profile) {
    withContext(Dispatchers.IO) {
      lock.withLock {
        if (!database.importedExists(profile.uuid)) {
          throw FileNotFoundException("profile ${profile.uuid} not found")
        }

        writeActiveProfileUuid(profile.uuid)
        syncActiveProfile(profile.uuid)
        refreshProfiles()
      }
    }
  }

  private fun generateProfileUuid(): Uuid {
    var uuid = uuidFactory()
    while (database.importedExists(uuid) || database.pendingExists(uuid)) {
      uuid = uuidFactory()
    }
    return uuid
  }

  private suspend fun fetchProfileConfiguration(
    source: String,
    profileDir: Path,
    force: Boolean,
  ): ProfileFetchResult? {
    val config = profileDir.resolve(CONFIGURATION_FILE)
    if (!profileShouldFetchConfiguration(source, force, config.exists())) return null

    return networkClient.fetchProfile(source).also { result ->
      profileDir.createDirectories()
      config.writeText(result.content)
    }
  }

  private suspend fun validateProfileDirectory(profileDir: Path) {
    val configFile = profileDir.resolve(CONFIGURATION_FILE)
    require(configFile.isRegularFile()) {
      "profile ${profileDir.name} does not contain config.yaml"
    }
    syncMihomoProviderCaches(
      profileDir = profileDir,
      mihomoHomeDir = profileDir,
      configFile = configFile,
    )
    configValidator?.validate(profileDir, configFile)
  }

  private fun syncActiveProfile(uuid: Uuid) {
    val sourceDir = importedDir.resolve(uuid.toString())
    val sourceConfig = sourceDir.resolve(CONFIGURATION_FILE)

    require(sourceConfig.isRegularFile()) { "profile $uuid does not contain config.yaml" }

    homeDir.createDirectories()
    Files.copy(sourceConfig, activeConfigFile, StandardCopyOption.REPLACE_EXISTING)
    syncMihomoProviderCaches(
      profileDir = sourceDir,
      mihomoHomeDir = homeDir,
      configFile = activeConfigFile,
    )
  }

  private fun deleteActiveProfile() {
    activeProfileFile.deleteRecursively()
    activeConfigFile.deleteRecursively()
  }

  private fun resolveProfile(uuid: Uuid): Profile? {
    val imported = database.queryImportedByUuid(uuid)
    val pending = database.queryPendingByUuid(uuid)
    val current = pending ?: imported ?: return null

    return Profile(
      uuid = uuid,
      name = current.name,
      type = current.type,
      source = current.source,
      active = imported != null && readActiveProfileUuid() == uuid,
      interval = current.interval,
      upload = current.upload,
      download = current.download,
      total = current.total,
      expire = current.expire,
      updatedAt = resolveUpdatedAt(uuid),
      imported = imported != null,
      pending = pending != null,
    )
  }

  private fun resolveUpdatedAt(uuid: Uuid): Long {
    return listOf(pendingDir, importedDir)
      .mapNotNull { root -> root.resolve(uuid.toString()).directoryLastModified() }
      .maxOrNull() ?: -1
  }

  private fun refreshProfiles() {
    profiles.value = loadProfiles()
  }

  private fun loadProfiles(): List<Profile> {
    val uuids = (database.queryImportedUuids() + database.queryPendingUuids()).distinct()
    return uuids.mapNotNull(::resolveProfile)
  }

  private fun readActiveProfileUuid(): Uuid? {
    if (activeProfileFile.notExists()) return null
    return runCatching { Uuid.parse(activeProfileFile.readText().trim()) }.getOrNull()
  }

  private fun writeActiveProfileUuid(uuid: Uuid) {
    homeDir.createDirectories()
    activeProfileFile.writeText(uuid.toString())
  }

  private fun ProfileEntity.enforceFieldValid() {
    val validationError =
      profileFieldValidationError(
        type = type,
        name = name,
        sourceMissing = source.isBlank(),
        sourceSupported =
          source.isBlank() || type == Profile.Type.File || isHttpProfileSource(source),
        interval = interval,
      )

    if (validationError == ProfileFieldValidationError.EmptyName) {
      throw IllegalArgumentException(profileFieldValidationErrorMessage(validationError, source))
    }

    if (type == Profile.Type.External) unsupported()

    validationError?.let { error ->
      throw IllegalArgumentException(profileFieldValidationErrorMessage(error, source))
    }
  }

  private fun ProfileEntity.withSubscriptionUserInfo(
    fetchedProfile: ProfileFetchResult?,
    createdAt: Long,
  ): ProfileEntity {
    val userInfo = fetchedProfile?.subscriptionUserInfo
    return copy(
      upload = userInfo?.upload ?: if (type == Profile.Type.Url) 0 else upload,
      download = userInfo?.download ?: if (type == Profile.Type.Url) 0 else download,
      total = userInfo?.total ?: if (type == Profile.Type.Url) 0 else total,
      expire = userInfo?.expire ?: if (type == Profile.Type.Url) 0 else expire,
      createdAt = createdAt,
    )
  }
}

private const val CONFIGURATION_FILE = "config.yaml"
internal const val PROVIDERS_DIR = "providers"

private fun Path.prepareNewProfileDirectory() {
  deleteRecursively()
  createDirectories()
  resolve(CONFIGURATION_FILE).writeText("")
  resolve(PROVIDERS_DIR).createDirectories()
}

private fun Path.deleteRecursively() {
  toFile().deleteRecursively()
}

private fun Path.directoryLastModified(): Long? {
  if (notExists()) return null
  return toFile().walk().maxOfOrNull { it.lastModified() }
}

private fun copyProfileDirectory(source: Path, target: Path) {
  if (source.notExists()) throw FileNotFoundException("profile path not found: $source")

  target.deleteRecursively()
  target.parent?.createDirectories()
  source.toFile().copyRecursively(target.toFile(), overwrite = true)
}
