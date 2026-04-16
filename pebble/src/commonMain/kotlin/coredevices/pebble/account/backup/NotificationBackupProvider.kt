package coredevices.pebble.account.backup

import coredevices.util.backup.BackupProvider
import io.rebble.libpebblecommon.database.dao.NotificationAppRealDao
import io.rebble.libpebblecommon.database.entity.NotificationAppItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Backup provider for notification settings (muted apps, patterns, colors).
 */
class NotificationBackupProvider(
    private val notificationAppDao: NotificationAppRealDao,
    private val json: Json
) : BackupProvider {
    override val backupKey: String = "notifications"

    override suspend fun exportData(): String {
        val apps = notificationAppDao.allApps()
        return json.encodeToString(apps)
    }

    override suspend fun importData(jsonString: String, merge: Boolean) {
        val apps = json.decodeFromString<List<NotificationAppItem>>(jsonString)
        if (merge) {
            notificationAppDao.insertOrReplace(apps)
        }
    }
}
