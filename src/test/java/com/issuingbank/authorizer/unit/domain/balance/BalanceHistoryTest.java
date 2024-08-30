package com.issuingbank.authorizer.unit.domain.balance;

import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceHistory;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BalanceHistoryTest {

    @Nested
    @DisplayName("set new balances in history")
    class NewBalancesTests {

    }

    @Nested
    @DisplayName("balance history creation")
    class CreationTests {
        @Test
        public void test_correct_balancehistory_creation() {
            // Given
            String accountNumber = "12345";

            Transaction transaction = Transaction.builder()
                    .account(accountNumber)
                    .amount(BigDecimal.valueOf(100))
                    .build();
            Balance previousBalance = Balance.builder()
                    .cashBalance(BigDecimal.valueOf(200))
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(30))
                    .build();

            Balance newBalance = Balance.builder()
                    .cashBalance(BigDecimal.valueOf(200))
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(30))
                    .build();

            // When
            BalanceHistory balanceHistory = BalanceHistory.create(previousBalance, newBalance, transaction);

            // Then
            assertNotNull(balanceHistory);
            assertEquals(transaction, balanceHistory.getTransaction());
            assertEquals(accountNumber, balanceHistory.getAccount());

            assertEquals(previousBalance.getCashBalance(), balanceHistory.getPreviousCashBalance());
            assertEquals(previousBalance.getFoodBalance(), balanceHistory.getPreviousFoodBalance());
            assertEquals(previousBalance.getMealBalance(), balanceHistory.getPreviousMealBalance());

            assertEquals(newBalance.getCashBalance(), balanceHistory.getNewCashBalance());
            assertEquals(newBalance.getFoodBalance(), balanceHistory.getNewFoodBalance());
            assertEquals(newBalance.getMealBalance(), balanceHistory.getNewMealBalance());
        }
    }
    
}
