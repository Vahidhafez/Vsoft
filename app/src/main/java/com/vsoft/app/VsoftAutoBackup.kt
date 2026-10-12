package com.vsoft.app

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.io.File
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "VsoftAutoBackupKey"
private const val BACKUP_PREFIX = "vsoft-backup-"
private const val LEGACY_BACKUP_PREFIX = "vsoft-auto-backup-"
private const val BACKUP_SUFFIX = ".enc"
private const val PUBLIC_BACKUP_ROOT = "Vsoft/Backups"
private const val PUBLIC_BACKUP_CACHE_PREFIX = "vsoft-public-backup-"
val BACKUP_RETENTION_KEY = stringPreferencesKey("backup_retention")

private data class PublicBackup(
    val uri: Uri,
    val name: String,
    val modified: Long,
    val size: Long
)

private fun vsoftSecretKey(): SecretKey {
    val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
    (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
    return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
        init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
    }.generateKey()
}

fun encryptVsoftAutoBackup(plain: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, vsoftSecretKey())
    val iv = cipher.iv
    return byteArrayOf(iv.size.toByte()) + iv + cipher.doFinal(plain)
}

fun decryptVsoftAutoBackup(payload: ByteArray): ByteArray {
    require(payload.isNotEmpty())
    val ivSize = payload[0].toInt() and 0xff
    require(ivSize in 12..16 && payload.size > ivSize + 1)
    val iv = payload.copyOfRange(1, 1 + ivSize)
    val encrypted = payload.copyOfRange(1 + ivSize, payload.size)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, vsoftSecretKey(), GCMParameterSpec(128, iv))
    return cipher.doFinal(encrypted)
}

private fun backupRetention(raw: String?): Int = when (raw?.toIntOrNull()) {
    3 -> 3
    5 -> 5
    10 -> 10
    else -> 3
}

private fun createBackupFileName(automatic: Boolean, now: Date = Date()): String {
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(now)
    val kind = if (automatic) "auto" else "manual"
    return "$BACKUP_PREFIX$kind-$stamp$BACKUP_SUFFIX"
}

private fun isBackupName(name: String): Boolean =
    name.endsWith(BACKUP_SUFFIX) &&
        (name.startsWith(BACKUP_PREFIX) || name.startsWith(LEGACY_BACKUP_PREFIX))

private fun backupFiles(directory: File): List<File> = directory.listFiles()
    .orEmpty()
    .filter { it.isFile && isBackupName(it.name) }
    .sortedByDescending { it.lastModified() }

private fun publicBackupCollection(context: Context): Uri =
    MediaStore.Files.getContentUri("external")

private fun listPublicBackups(context: Context): List<PublicBackup> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return emptyList()
    val result = mutableListOf<PublicBackup>()
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.RELATIVE_PATH
    )
    runCatching {
        context.contentResolver.query(
            publicBackupCollection(context),
            projection,
            "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?",
            arrayOf("${Environment.DIRECTORY_DOCUMENTS}/$PUBLIC_BACKUP_ROOT/%"),
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn) ?: continue
                if (!isBackupName(name)) continue
                result += PublicBackup(
                    Uri.withAppendedPath(publicBackupCollection(context), cursor.getLong(idColumn).toString()),
                    name,
                    cursor.getLong(modifiedColumn) * 1000L,
                    cursor.getLong(sizeColumn)
                )
            }
        }
    }
    return result
}

private fun pruneOldBackups(context: Context, keep: Int) {
    val privateFiles = backupFiles(context.filesDir)
    val publicFiles = listPublicBackups(context)
    data class Candidate(val modified: Long, val privateFile: File? = null, val publicFile: PublicBackup? = null)
    val all = (privateFiles.map { Candidate(it.lastModified(), privateFile = it) } +
        publicFiles.map { Candidate(it.modified, publicFile = it) }).sortedByDescending { it.modified }
    all.drop(keep).forEach { candidate ->
        candidate.privateFile?.delete()
        candidate.publicFile?.let { runCatching { context.contentResolver.delete(it.uri, null, null) } }
    }
}

fun pruneStoredVsoftBackups(context: Context, keep: Int) {
    require(keep == 3 || keep == 5 || keep == 10)
    pruneOldBackups(context, keep)
}

/**
 * On Android 10+, stores encrypted backups in the public Documents/Vsoft/Backups tree.
 * Older Android versions retain app-private storage to avoid requesting broad storage access.
 */
suspend fun createStoredVsoftBackup(context: Context, backup: VsoftBackup, automatic: Boolean): File {
    val name = createBackupFileName(automatic)
    val encrypted = encryptVsoftAutoBackup(encodeBackup(backup).toByteArray(Charsets.UTF_8))
    val storedFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val subfolder = if (automatic) "Automatic" else "Manual"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$PUBLIC_BACKUP_ROOT/$subfolder")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(publicBackupCollection(context), values)
            ?: error("Could not create backup file in Documents")
        try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(encrypted) }
                ?: error("Could not open backup output stream")
            val ready = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            context.contentResolver.update(uri, ready, null, null)
            true
        } catch (error: Throwable) {
            context.contentResolver.delete(uri, null, null)
            throw error
        }
    } else {
        File(context.filesDir, name).writeBytes(encrypted)
        false
    }
    val prefs = context.dataStore.data.first()
    pruneOldBackups(context, backupRetention(prefs[BACKUP_RETENTION_KEY]))
    // Return a temporary readable copy so existing restore UI can continue using File.
    return if (storedFile) {
        val temp = File(context.cacheDir, "$PUBLIC_BACKUP_CACHE_PREFIX$name")
        temp.writeBytes(encrypted)
        temp.setLastModified(System.currentTimeMillis())
        temp
    } else {
        File(context.filesDir, name)
    }
}

/** Lists encrypted backups newest first, including older app-private backups for migration. */
fun listStoredVsoftBackups(context: Context): List<File> {
    val cache = context.cacheDir.listFiles().orEmpty()
    cache.filter { it.name.startsWith(PUBLIC_BACKUP_CACHE_PREFIX) }.forEach { it.delete() }
    val privateFiles = backupFiles(context.filesDir)
    val publicFiles = listPublicBackups(context).mapNotNull { entry ->
        runCatching {
            val target = File(context.cacheDir, "$PUBLIC_BACKUP_CACHE_PREFIX${entry.name}")
            context.contentResolver.openInputStream(entry.uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Could not read backup")
            target.setLastModified(entry.modified)
            target
        }.getOrNull()
    }
    return (privateFiles + publicFiles).sortedByDescending { it.lastModified() }
}

/** Opens and validates an encrypted stored backup. */
fun readStoredVsoftBackup(file: File): VsoftBackup? = runCatching {
    decodeBackup(String(decryptVsoftAutoBackup(file.readBytes()), Charsets.UTF_8))
}.getOrNull()

class VsoftAutoBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val p = applicationContext.dataStore.data.first()
        val transactions = decodeTransactions(p[TRANSACTIONS_KEY] ?: "[]")
        val workDays = decodeWork(p[WORK_KEY] ?: "[]")
        val cards = decodeCards(p[CARDS_KEY] ?: "[]", transactions)
        val backup = VsoftBackup(
            transactions,
            workDays,
            cards,
            decodePeople(p[PEOPLE_KEY] ?: "[]"),
            decodeWorkplaces(p[WORKPLACES_KEY] ?: "[]"),
            decodeWorkPurchases(p[WORK_PURCHASES_KEY] ?: "[]"),
            p[LANGUAGE_KEY] ?: "fa",
            p[CURRENCY_KEY] ?: "IRT",
            p[THEME_KEY] ?: "system",
            p[GLASS_KEY]?.toBoolean() ?: false,
            p[FONT_KEY] ?: "sans"
        )
        createStoredVsoftBackup(applicationContext, backup, automatic = true)
        Result.success()
    }.getOrElse { Result.retry() }
}
