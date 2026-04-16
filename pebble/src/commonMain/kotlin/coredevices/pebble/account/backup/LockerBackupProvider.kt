package coredevices.pebble.account.backup

import coredevices.util.backup.BackupProvider
import io.rebble.libpebblecommon.database.dao.LockerEntryRealDao
import io.rebble.libpebblecommon.database.entity.LockerEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Backup provider for the watch's locker (installed apps and watchfaces).
 */
class LockerBackupProvider(
    private val lockerEntryDao: LockerEntryRealDao,
    private val json: Json,
    private val lockerSyncLimit: Int
) : BackupProvider {
    override val backupKey: String = "locker"

    override suspend fun exportData(): String {
        val entries = lockerEntryDao.getAllEntries()
        return json.encodeToString(entries)
    }

    override suspend fun importData(jsonString: String, merge: Boolean) {
        val entries = json.decodeFromString<List<LockerEntry>>(jsonString)
        if (merge) {
            lockerEntryDao.insertOrReplaceAndOrder(entries, lockerSyncLimit)
        }
    }
}
