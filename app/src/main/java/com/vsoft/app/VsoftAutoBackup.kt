package com.vsoft.app

import android.content.Context
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
private const val BACKUP_PREFIX = "vsoft-auto-backup-"
private const val BACKUP_SUFFIX = ".enc"
private val BACKUP_RETENTION_KEY = stringPreferencesKey("backup_retention")

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

private fun createBackupFileName(now: Date = Date()): String {
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(now)
    return "$BACKUP_PREFIX$stamp$BACKUP_SUFFIX"
}

private fun pruneOldBackups(directory: File, keep: Int) {
    val backups = directory.listFiles()
        .orEmpty()
        .filter { it.isFile && it.name.startsWith(BACKUP_PREFIX) && it.name.endsWith(BACKUP_SUFFIX) }
        .sortedByDescending { it.lastModified() }
    backups.drop(keep).forEach { it.delete() }
}

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
        val directory = applicationContext.filesDir
        val target = File(directory, createBackupFileName())
        val encrypted = encryptVsoftAutoBackup(encodeBackup(backup).toByteArray(Charsets.UTF_8))
        target.writeBytes(encrypted)
        pruneOldBackups(directory, backupRetention(p[BACKUP_RETENTION_KEY]))
        Result.success()
    }.getOrElse { Result.retry() }
}
