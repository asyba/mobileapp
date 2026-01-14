package coredevices.pebble.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.languageIdentifier
import platform.Speech.SFSpeechRecognizer

@OptIn(ExperimentalComposeUiApi::class)
actual fun makeTokenClipEntry(token: String): ClipEntry = ClipEntry.withPlainText(token)
actual fun getPlatformSTTLanguages(): List<Pair<String, String>> {
    @Suppress("UNCHECKED_CAST")
    val locales = SFSpeechRecognizer.supportedLocales() as Set<NSLocale>
    return locales.filter {
        SFSpeechRecognizer(it).supportsOnDeviceRecognition
    }.map { locale ->
        val code = locale.languageIdentifier
        val name = locale.displayNameForKey(NSLocaleIdentifier, value = code) ?: code
        code to name
    }.sortedBy { it.second }
}

@OptIn(ExperimentalForeignApi::class)
actual fun getCacheDir(): String {
    val cacheDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return requireNotNull(cacheDirectory?.path)
}