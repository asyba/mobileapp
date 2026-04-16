package coredevices.util.backup

import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * Orchestrates the backup and restore process by coordinating multiple [BackupProvider]s.
 */
class BackupManager(
    private val providers: List<BackupProvider>,
    private val json: Json
) {
    private val logger = Logger.withTag("BackupManager")

    /**
     * Consolidates data from all providers into a single JSON string.
     */
    suspend fun createBackupJson(): String {
        return buildJsonObject {
            providers.forEach { provider ->
                try {
                    val data = provider.exportData()
                    put(provider.backupKey, json.parseToJsonElement(data))
                } catch (e: Exception) {
                    logger.e(e) { "Failed to export data for provider: ${provider.backupKey}" }
                }
            }
        }.toString()
    }

    /**
     * Parses a consolidated JSON backup and distributes data to each provider.
     */
    suspend fun restoreFromBackupJson(backupJson: String, merge: Boolean = true) {
        val root = try {
            json.parseToJsonElement(backupJson).jsonObject
        } catch (e: Exception) {
            logger.e(e) { "Failed to parse backup JSON" }
            return
        }

        providers.forEach { provider ->
            root[provider.backupKey]?.let { element ->
                try {
                    provider.importData(element.toString(), merge)
                } catch (e: Exception) {
                    logger.e(e) { "Failed to import data for provider: ${provider.backupKey}" }
                }
            }
        }
    }
}
