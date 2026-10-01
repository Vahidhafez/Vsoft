package com.vsoft.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.io.File
import java.security.KeyStore
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "VsoftAutoBackupKey"
private const val BACKUP_FILE = "vsoft-auto-backup.enc"

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

class VsoftAutoBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val p = applicationContext.dataStore.data.first()
        val backup = VsoftBackup(
            decodeTransactions(p[TRANSACTIONS_KEY] ?: "[]"),
            decodeWork(p[WORK_KEY] ?: "[]"),
            decodeCards(p[CARDS_KEY] ?: "[]"),
            decodePeople(p[PEOPLE_KEY] ?: "[]"),
            decodeWorkplaces(p[WORKPLACES_KEY] ?: "[]"),
            p[LANGUAGE_KEY] ?: "fa",
            p[CURRENCY_KEY] ?: "IRT",
            p[THEME_KEY] ?: "system",
            p[GLASS_KEY]?.toBoolean() ?: false,
            p[FONT_KEY] ?: "sans"
        )
        val encrypted = encryptVsoftAutoBackup(encodeBackup(backup).toByteArray(Charsets.UTF_8))
        File(applicationContext.filesDir, BACKUP_FILE).writeBytes(encrypted)
        Result.success()
    }.getOrElse { Result.retry() }
}
