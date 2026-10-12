package com.vsoft.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FinancialRegressionTest {
    private fun transaction(type: String, amount: Long, card: String = "کارت اصلی") =
        Transaction(
            id = 1L,
            type = type,
            amount = amount,
            category = "آزمایشی",
            description = "تراکنش آزمایشی",
            date = "1405/07/20",
            card = card,
            person = ""
        )

    private fun card(opening: Long = 15_000_000L, name: String = "کارت اصلی") =
        BankCard(1L, "بانک ملی", name, "", "", opening)

    @Test
    fun openingBalanceRemainsFixedAndExpenseChangesOnlyCurrentBalance() {
        val bankCard = card()
        val expense = transaction("expense", 10_000_000L)

        assertEquals(15_000_000L, bankCard.openingBalance)
        assertEquals(5_000_000L, cardCurrentBalance(bankCard, listOf(expense), emptyList()))
        assertEquals(15_000_000L, bankCard.openingBalance)
    }

    @Test
    fun incomeAndExpenseAreNetMovementNotReplacementBalances() {
        val bankCard = card(100_000L)
        val transactions = listOf(
            transaction("income", 500_000L),
            transaction("expense", 125_000L)
        )

        assertEquals(475_000L, cardCurrentBalance(bankCard, transactions, emptyList()))
        assertEquals(100_000L, bankCard.openingBalance)
    }

    @Test
    fun transactionsOnAnotherCardDoNotAffectThisCardBalance() {
        val bankCard = card()
        val transactions = listOf(
            transaction("income", 900_000L, "کارت دیگر"),
            transaction("expense", 200_000L, "کارت دیگر")
        )

        assertEquals(15_000_000L, cardCurrentBalance(bankCard, transactions, emptyList()))
    }

    @Test
    fun workIncomeIsIncludedOnlyForItsSelectedCard() {
        val bankCard = card(100_000L)
        val workDays = listOf(
            WorkDay(1L, "کارگاه", "1405/07/20", "08:00", "12:00", 300_000L, "", "", card = "کارت اصلی"),
            WorkDay(2L, "پروژه", "1405/07/20", "13:00", "15:00", 700_000L, "", "", card = "کارت دیگر")
        )

        assertEquals(400_000L, cardCurrentBalance(bankCard, emptyList(), workDays))
    }

    @Test
    fun newCardFormatPreservesManualOpeningBalanceEvenWithOldSmsRows() {
        val bankCard = card(15_000_000L)
        val encoded = encodeCards(listOf(bankCard))
        val oldSms = transaction("expense", 10_000_000L).copy(
            description = "ثبت خودکار از پیامک بانک"
        )

        assertEquals(15_000_000L, decodeCards(encoded, listOf(oldSms)).single().openingBalance)
    }

    @Test
    fun personDebtAmountsSurvivePersistenceRoundTrip() {
        val person = Person(
            id = 9L,
            name = "آزمایش",
            phone = "",
            note = "",
            amountTheyOweMe = 250_000L,
            amountIOwe = 75_000L
        )

        val restored = decodePeople(encodePeople(listOf(person))).single()
        assertEquals(250_000L, restored.amountTheyOweMe)
        assertEquals(75_000L, restored.amountIOwe)
        assertNotEquals(restored.amountTheyOweMe, restored.amountIOwe)
    }
}
