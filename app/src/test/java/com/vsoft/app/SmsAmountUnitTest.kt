package com.vsoft.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SmsAmountUnitTest {
    @Test
    fun rialTransactionIsNotMultipliedWhenSmsAlsoMentionsToman() {
        val parsed = parseBankSms(
            sender = "BankMelli",
            rawBody = "خرید مبلغ 100,000 ریال مانده 500,000 ریال معادل 50,000 تومان کارت ****1234",
            time = 1_760_000_000_000L
        )

        assertNotNull(parsed)
        assertEquals(100_000L, parsed!!.amountRial)
        assertEquals(500_000L, parsed.balanceRial)
    }

    @Test
    fun tomanTransactionIsConvertedIndependentlyOfRialBalance() {
        val parsed = parseBankSms(
            sender = "BankMelli",
            rawBody = "خرید مبلغ 50,000 تومان مانده 500,000 ریال کارت ****1234",
            time = 1_760_000_000_001L
        )

        assertNotNull(parsed)
        assertEquals(500_000L, parsed!!.amountRial)
        assertEquals(500_000L, parsed.balanceRial)
    }
}
