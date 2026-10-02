package ca.tommysanterre.snackloop

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val RELEASES_API_URL =
    "https://api.github.com/repos/tommysanterre/hyperbolic-chamber/releases/latest"

data class AvailableUpdate(val version: String, val releaseUrl: String)

suspend fun checkForUpdate(context: Context): AvailableUpdate? = withContext(Dispatchers.IO) {
    try {
        @Suppress("DEPRECATION")
        val installedVersion = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName ?: return@withContext null
        val connection = (URL(RELEASES_API_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Hyperbolic-Chamber")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val release = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            val version = release.optString("tag_name")
            val releaseUrl = release.optString("html_url")
            if (isNewerVersion(version, installedVersion) &&
                releaseUrl.startsWith("https://github.com/tommysanterre/hyperbolic-chamber/releases/tag/")) {
                AvailableUpdate(version, releaseUrl)
            } else null
        } finally {
            connection.disconnect()
        }
    } catch (_: IOException) {
        null
    } catch (_: JSONException) {
        null
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}

internal fun isNewerVersion(latest: String, installed: String): Boolean {
    val latestParts = parseVersion(latest) ?: return false
    val installedParts = parseVersion(installed) ?: return false
    for (index in latestParts.indices) {
        if (latestParts[index] != installedParts[index]) {
            return latestParts[index] > installedParts[index]
        }
    }
    return false
}

private fun parseVersion(value: String): List<Long>? {
    val parts = value.removePrefix("v").split('.')
    if (parts.size != 3) return null
    return parts.map { it.toLongOrNull() ?: return null }
}
