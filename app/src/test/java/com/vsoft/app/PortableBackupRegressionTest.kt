package com.vsoft.app

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
class PortableBackupRegressionTest {
    private fun sampleBackup() = VsoftBackup(
        transactions = listOf(
            Transaction(7L, "expense", 123_456L, "خرید", "نمونه", "1405/07/20", "کارت اصلی", "")
        ),
        workDays = emptyList(),
        cards = listOf(BankCard(1L, "بانک ملی", "کارت اصلی", "", "1234", 2_000_000L)),
        people = listOf(Person(2L, "فرد آزمایشی", "", "", amountTheyOweMe = 50_000L)),
        workplaces = listOf(Workplace(3L, "کارگاه")),
        workPurchases = listOf(WorkPurchase(4L, "ابزار", 80_000L, "مصالح", "1405/07/20", "کارگاه", "کارت اصلی")),
        language = "fa",
        currency = "IRT",
        theme = "system",
        glass = true,
        font = "sans"
    )

    @Test
    fun encryptedPortableBackupRoundTripsAllUserData() {
        val password = "a-long-test-password".toCharArray()
        val restored = decodePortableVsoftBackup(encodePortableVsoftBackup(sampleBackup(), password), password)
        assertNotNull(restored)
        assertEquals(sampleBackup().transactions, restored!!.transactions)
        assertEquals(sampleBackup().cards, restored.cards)
        assertEquals(sampleBackup().people, restored.people)
        assertEquals(sampleBackup().workplaces, restored.workplaces)
        assertEquals(sampleBackup().workPurchases, restored.workPurchases)
        assertEquals("fa", restored.language)
        assertEquals("IRT", restored.currency)
        assertEquals("system", restored.theme)
        assertEquals(true, restored.glass)
        assertEquals("sans", restored.font)
    }

    @Test
    fun wrongPasswordCannotDecodePortableBackup() {
        val encrypted = encodePortableVsoftBackup(sampleBackup(), "correct-password".toCharArray())
        assertNull(decodePortableVsoftBackup(encrypted, "incorrect-password".toCharArray()))
    }

    @Test
    fun corruptedAndTruncatedPortableBackupsAreRejected() {
        val encrypted = encodePortableVsoftBackup(sampleBackup(), "a-long-test-password".toCharArray())
        val corrupted = encrypted.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 0x01).toByte() }
        assertNull(decodePortableVsoftBackup(corrupted, "a-long-test-password".toCharArray()))
        assertNull(decodePortableVsoftBackup(encrypted.copyOf(encrypted.size - 5), "a-long-test-password".toCharArray()))
        assertNull(decodePortableVsoftBackup(byteArrayOf(1, 2, 3), "a-long-test-password".toCharArray()))
    }

    @Test
    fun shortPasswordsAreRejectedOnExportAndImport() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            encodePortableVsoftBackup(sampleBackup(), "short".toCharArray())
        }
        assertNull(decodePortableVsoftBackup(byteArrayOf(1, 2, 3), "short".toCharArray()))
    }
}
