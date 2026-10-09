package com.vsoft.app

import org.junit.Assert.assertEquals
import org.junit.Test

class OpeningBalanceMigrationTest {
    private fun transaction(
        type: String,
        amount: Long,
        date: String,
        card: String = "کارت اصلی",
        description: String = "تراکنش دستی"
    ) = Transaction(
        id = 1L,
        type = type,
        amount = amount,
        category = "",
        description = description,
        date = date,
        card = card,
        person = ""
    )

    @Test
    fun preservesManualOpeningBalanceWhenNoLegacySmsWasImported() {
        val transactions = listOf(
            transaction("expense", 10_000_000L, "1405/07/09")
        )

        assertEquals(
            15_000_000L,
            recoverLegacyOpeningBalance("کارت اصلی", 15_000_000L, transactions)
        )
    }

    @Test
    fun reconstructsOpeningBalanceWhenLegacySmsStoredCurrentBalance() {
        val transactions = listOf(
            transaction(
                type = "expense",
                amount = 10_000_000L,
                date = "1405/07/09",
                description = "ثبت خودکار از پیامک بانک"
            )
        )

        // The old app replaced the card balance with the SMS-reported 5M.
        // Reverse the SMS expense to recover the manually entered 15M opening balance.
        assertEquals(
            15_000_000L,
            recoverLegacyOpeningBalance("کارت اصلی", 5_000_000L, transactions)
        )
    }

    @Test
    fun doesNotSubtractTransactionsRecordedAfterTheLastLegacySms() {
        val transactions = listOf(
            transaction(
                type = "expense",
                amount = 10_000_000L,
                date = "1405/07/08",
                description = "ثبت خودکار از پیامک بانک"
            ),
            transaction(
                type = "expense",
                amount = 2_000_000L,
                date = "1405/07/09"
            )
        )

        assertEquals(
            15_000_000L,
            recoverLegacyOpeningBalance("کارت اصلی", 5_000_000L, transactions)
        )
    }


    @Test
    fun usesLastConfirmedSmsEvenWhenItsMessageDateIsOlder() {
        val firstConfirmation = System.currentTimeMillis() - 20_000L
        val lastConfirmation = System.currentTimeMillis() - 10_000L
        val transactions = listOf(
            transaction(
                type = "expense",
                amount = 10_000_000L,
                date = "1405/07/09",
                description = "ثبت خودکار از پیامک بانک"
            ).copy(id = firstConfirmation),
            transaction(
                type = "expense",
                amount = 3_000_000L,
                date = "1405/07/01",
                description = "ثبت خودکار از پیامک بانک"
            ).copy(id = lastConfirmation)
        )

        // The older message was confirmed last, so its 12M reported balance overwrote
        // the card value. Only the 3M movement through that older SMS belongs in recovery.
        assertEquals(
            15_000_000L,
            recoverLegacyOpeningBalance("کارت اصلی", 12_000_000L, transactions)
        )
    }

    @Test
    fun excludesSameDayTransactionsCreatedAfterTheLastSmsConfirmation() {
        val smsConfirmation = System.currentTimeMillis() - 10_000L
        val laterManualTransaction = System.currentTimeMillis()
        val transactions = listOf(
            transaction(
                type = "expense",
                amount = 10_000_000L,
                date = "1405/07/09",
                description = "ثبت خودکار از پیامک بانک"
            ).copy(id = smsConfirmation),
            transaction(
                type = "expense",
                amount = 2_000_000L,
                date = "1405/07/09"
            ).copy(id = laterManualTransaction)
        )

        assertEquals(
            15_000_000L,
            recoverLegacyOpeningBalance("کارت اصلی", 5_000_000L, transactions)
        )
    }

    @Test
    fun ignoresTransactionsBelongingToAnotherCard() {
        val transactions = listOf(
            transaction(
                type = "expense",
                amount = 10_000_000L,
                date = "1405/07/09",
                card = "کارت دیگر",
                description = "ثبت خودکار از پیامک بانک"
            )
        )

        assertEquals(
            15_000_000L,
            recoverLegacyOpeningBalance("کارت اصلی", 15_000_000L, transactions)
        )
    }
}
