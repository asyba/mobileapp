package coredevices.util.backup

/**
 * Interface for modules that provide data for the local backup system.
 */
interface BackupProvider {
    /**
     * The unique key for this module's data in the backup JSON.
     */
    val backupKey: String

    /**
     * Exports the module's data as a JSON string.
     */
    suspend fun exportData(): String

    /**
     * Imports the module's data from a JSON string.
     * Use [merge] to decide if we should overwrite or just add missing items.
     */
    suspend fun importData(json: String, merge: Boolean)
}
