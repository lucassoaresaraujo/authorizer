package com.issuingbank.authorizer.unit.application.debits;

import com.issuingbank.authorizer.application.debits.DebitProcessor;
import com.issuingbank.authorizer.application.debits.DebitResult;
import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceType;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import com.issuingbank.authorizer.domain.transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DebitProcessorTest {
    @Test
    public void test_debit_processed_when_sufficient_balance() {
        // Given
        BigDecimal initialFoodBalance = BigDecimal.valueOf(100);
        BigDecimal initialMealBalance = BigDecimal.valueOf(150);
        BigDecimal initialCashBalance = BigDecimal.valueOf(350);

        BigDecimal transactionAmount = BigDecimal.valueOf(50);

        Balance currentBalance = Balance.builder()
                .account("1")
                .foodBalance(initialFoodBalance)
                .mealBalance(initialMealBalance)
                .cashBalance(initialCashBalance)
                .build();

        BalanceType sourceBalanceType = BalanceType.FOOD;

        Transaction requestedTransaction = Transaction.builder()
                .amount(transactionAmount)
                .type(TransactionType.DEBIT)
                .build();

        // When
        Optional<DebitResult> result = new DebitProcessor().doDebit(currentBalance, sourceBalanceType, requestedTransaction);

        // Then
        BigDecimal expectedFoodBalance = initialFoodBalance.subtract(transactionAmount);

        assertTrue(result.isPresent());

        DebitResult debitResult = result.get();

        assertEquals(expectedFoodBalance, debitResult.newBalance().getFoodBalance());
        assertEquals(initialMealBalance, debitResult.newBalance().getMealBalance());
        assertEquals(initialCashBalance, debitResult.newBalance().getCashBalance());

        assertNotEquals(currentBalance.getFoodBalance(), debitResult.newBalance().getFoodBalance());
    }

    @Test
    public void test_history_when_sufficient_balance() {
        // Given
        BigDecimal initialFoodBalance = BigDecimal.valueOf(100);
        BigDecimal initialMealBalance = BigDecimal.valueOf(150);
        BigDecimal initialCashBalance = BigDecimal.valueOf(350);

        BigDecimal transactionAmount = BigDecimal.valueOf(50);

        Balance currentBalance = Balance.builder()
                .account("1")
                .foodBalance(initialFoodBalance)
                .mealBalance(initialMealBalance)
                .cashBalance(initialCashBalance)
                .build();

        BalanceType sourceBalanceType = BalanceType.FOOD;

        Transaction requestedTransaction = Transaction.builder()
                .amount(transactionAmount)
                .type(TransactionType.DEBIT)
                .build();

        // When
        Optional<DebitResult> result = new DebitProcessor().doDebit(currentBalance, sourceBalanceType, requestedTransaction);

        // Then
        BigDecimal expectedFoodBalance = initialFoodBalance.subtract(transactionAmount);

        assertTrue(result.isPresent());

        DebitResult debitResult = result.get();

        assertEquals(initialMealBalance, debitResult.balanceHistory().getPreviousMealBalance());
        assertEquals(initialMealBalance, debitResult.balanceHistory().getNewMealBalance());

        assertEquals(initialCashBalance, debitResult.balanceHistory().getPreviousCashBalance());
        assertEquals(initialCashBalance, debitResult.balanceHistory().getNewCashBalance());

        assertEquals(initialFoodBalance, debitResult.balanceHistory().getPreviousFoodBalance());
        assertEquals(expectedFoodBalance, debitResult.balanceHistory().getNewFoodBalance());
    }

    @Test
    public void test_debit_not_processed_when_insufficient_balance() {
        // Given
        Balance currentBalance = Balance.builder()
                .cashBalance(BigDecimal.ZERO)
                .mealBalance(BigDecimal.ZERO)
                .foodBalance(BigDecimal.TEN)
                .build();

        BalanceType sourceBalanceType = BalanceType.FOOD;

        BigDecimal transactionAmount = BigDecimal.valueOf(50);

        Transaction requestedTransaction = Transaction.builder()
                .amount(transactionAmount)
                .type(TransactionType.DEBIT)
                .build();

        // When
        Optional<DebitResult> result = new DebitProcessor().doDebit(currentBalance, sourceBalanceType, requestedTransaction);

        // Then
        assertFalse(result.isPresent());
    }
}
