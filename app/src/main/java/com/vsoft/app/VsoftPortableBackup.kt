package com.vsoft.app

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-protected portable backup format. Unlike AndroidKeyStore-encrypted local
 * backups, this format can be decrypted after reinstall on another device with the password.
 */
private val PORTABLE_MAGIC = byteArrayOf(0x56, 0x53, 0x50, 0x42, 0x01) // VSPB + version
private const val PORTABLE_SALT_SIZE = 16
private const val PORTABLE_IV_SIZE = 12
private const val PORTABLE_ITERATIONS = 210_000
private const val PORTABLE_KEY_BITS = 256

private fun derivePortableBackupKey(password: CharArray, salt: ByteArray): SecretKeySpec {
    require(password.size >= 10) { "Backup password must contain at least 10 characters" }
    val spec = PBEKeySpec(password, salt, PORTABLE_ITERATIONS, PORTABLE_KEY_BITS)
    return try {
        val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        SecretKeySpec(raw, "AES")
    } finally {
        spec.clearPassword()
    }
}

fun encodePortableVsoftBackup(backup: VsoftBackup, password: CharArray): ByteArray {
    require(password.size >= 10) { "Backup password must contain at least 10 characters" }
    val random = SecureRandom()
    val salt = ByteArray(PORTABLE_SALT_SIZE).also(random::nextBytes)
    val iv = ByteArray(PORTABLE_IV_SIZE).also(random::nextBytes)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, derivePortableBackupKey(password, salt), GCMParameterSpec(128, iv))
    cipher.updateAAD(PORTABLE_MAGIC)
    val encrypted = cipher.doFinal(encodeBackup(backup).toByteArray(Charsets.UTF_8))
    return ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { out ->
            out.write(PORTABLE_MAGIC)
            out.writeByte(salt.size)
            out.write(salt)
            out.writeByte(iv.size)
            out.write(iv)
            out.writeInt(encrypted.size)
            out.write(encrypted)
        }
        bytes.toByteArray()
    }
}

fun decodePortableVsoftBackup(payload: ByteArray, password: CharArray): VsoftBackup? = runCatching {
    require(password.size >= 10) { "Backup password must contain at least 10 characters" }
    DataInputStream(ByteArrayInputStream(payload)).use { input ->
        val magic = ByteArray(PORTABLE_MAGIC.size)
        input.readFully(magic)
        require(magic.contentEquals(PORTABLE_MAGIC)) { "Unsupported portable backup format" }
        val saltSize = input.readUnsignedByte()
        require(saltSize == PORTABLE_SALT_SIZE)
        val salt = ByteArray(saltSize).also(input::readFully)
        val ivSize = input.readUnsignedByte()
        require(ivSize == PORTABLE_IV_SIZE)
        val iv = ByteArray(ivSize).also(input::readFully)
        val encryptedSize = input.readInt()
        require(encryptedSize in 17..(payload.size - PORTABLE_MAGIC.size - 2 - saltSize - ivSize - 4))
        val encrypted = ByteArray(encryptedSize).also(input::readFully)
        require(input.read() == -1) { "Unexpected trailing data" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, derivePortableBackupKey(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(PORTABLE_MAGIC)
        decodeBackup(String(cipher.doFinal(encrypted), Charsets.UTF_8))
    }
}.getOrNull()
