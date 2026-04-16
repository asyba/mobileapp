package coredevices.pebble.account.backup

import coredevices.util.CoreConfig
import coredevices.util.CoreConfigHolder
import coredevices.util.backup.BackupProvider
import io.rebble.libpebblecommon.LibPebbleConfig
import io.rebble.libpebblecommon.LibPebbleConfigHolder
import io.rebble.libpebblecommon.database.entity.AppPrefsEntry
import io.rebble.libpebblecommon.database.entity.WatchPrefItem
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock
import io.rebble.libpebblecommon.database.MillisecondInstant

@Serializable
data class CombinedSettingsBackup(
    val coreConfig: CoreConfig,
    val libPebbleConfig: LibPebbleConfig,
    val watchPrefs: List<WatchPrefItem> = emptyList(),
    val appPrefs: List<AppPrefsEntry> = emptyList()
)

/**
 * Backup provider for app-wide settings (Theme, intervals, notifications config, etc).
 */
class SettingsBackupProvider(
    private val coreConfigHolder: CoreConfigHolder,
    private val libPebble: io.rebble.libpebblecommon.connection.LibPebble,
    private val json: Json
) : BackupProvider {
    override val backupKey: String = "settings"

    override suspend fun exportData(): String {
        val backup = CombinedSettingsBackup(
            coreConfig = coreConfigHolder.config.value,
            libPebbleConfig = libPebble.config.value,
            watchPrefs = libPebble.database.watchPrefDao().getAllFlow().first(),
            appPrefs = libPebble.database.appPrefsDao().getAll()
        )
        return json.encodeToString(backup)
    }

    override suspend fun importData(jsonString: String, merge: Boolean) {
        val backup = json.decodeFromString<CombinedSettingsBackup>(jsonString)
        if (merge) {
            // In Merge mode for settings, we actually just overwrite the fields that are in the backup
            // Since these are entire objects, we just update the holders.
            coreConfigHolder.update(backup.coreConfig)
            libPebble.updateConfig(backup.libPebbleConfig)

            val nowTimestamp = MillisecondInstant(Clock.System.now().toEpochMilliseconds())
            val refreshedWatchPrefs = backup.watchPrefs.map { it.copy(timestamp = nowTimestamp) }
            
            libPebble.database.watchPrefDao().insertOrReplace(refreshedWatchPrefs)
            libPebble.database.appPrefsDao().insertOrReplace(backup.appPrefs)
        }
    }
}
