package com.issuingbank.authorizer.unit.domain.balance;

import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

public class BalanceTest {

    @Nested
    @DisplayName("get balance tests")
    class GetBalanceTests {
        @Test
        public void test_retrieve_food_balance_successfully() {
            // Giver
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(30))
                    .cashBalance(BigDecimal.valueOf(100))
                    .build();

            // When
            BigDecimal retrievedFoodBalance = balance.getBalance(BalanceType.FOOD);

            // Then
            assertEquals(BigDecimal.valueOf(50), retrievedFoodBalance);
        }

        @Test
        public void test_retrieve_meal_balance_successfully() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(100))
                    .cashBalance(BigDecimal.valueOf(200))
                    .build();

            // When
            BigDecimal retrievedMealBalance = balance.getBalance(BalanceType.MEAL);

            // Then
            assertEquals(BigDecimal.valueOf(100), retrievedMealBalance);
        }

        @Test
        public void retrieve_cash_balance_successfully() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.ONE)
                    .cashBalance(BigDecimal.valueOf(100))
                    .mealBalance(BigDecimal.ZERO)
                    .build();

            // When
            BigDecimal retrievedBalance = balance.getBalance(BalanceType.CASH);

            // Then
            assertEquals(BigDecimal.valueOf(100), retrievedBalance);
        }
    }

    @Nested
    @DisplayName("setting balance tests")
    class SettingBalanceTests {
        @Test
        public void test_setting_valid_balance() {
            // Given
            Balance balance = Balance.builder().build();
            BigDecimal amount = BigDecimal.valueOf(100);

            // When
            balance.setBalance(BalanceType.FOOD, amount);

            // Then
            assertEquals(amount, balance.getBalance(BalanceType.FOOD));
        }

        @Test
        public void test_updating_existing_balance() {
            // Given
            BigDecimal initialAmount = BigDecimal.valueOf(50);
            BigDecimal newAmount = BigDecimal.valueOf(150);
            Balance balance = Balance.builder()
                    .mealBalance(initialAmount)
                    .build();

            // When
            balance.setBalance(BalanceType.MEAL, newAmount);

            // Then
            assertEquals(newAmount, balance.getBalance(BalanceType.MEAL));
        }

        @Test
        public void test_setting_balance_to_zero() {
            // Given
            Balance balance = Balance.builder().build();
            BigDecimal zeroAmount = BigDecimal.ZERO;

            // When
            balance.setBalance(BalanceType.CASH, zeroAmount);

            // Then
            assertEquals(zeroAmount, balance.getBalance(BalanceType.CASH));
        }

        @Test
        public void test_setting_negative_balance_throws_exception() {
            // Given
            Balance balance = Balance.builder().build();
            BigDecimal negativeAmount = BigDecimal.valueOf(-100);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                balance.setBalance(BalanceType.FOOD, negativeAmount);
            });
        }
    }

    @Nested
    @DisplayName("has sufficient balance tests")
    class SufficientBalanceTests {

        @Test
        public void returns_true_when_balance_is_greater_than_amount() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(100))
                    .build();

            // When
            boolean result = balance.hasSufficientBalance(BalanceType.FOOD, BigDecimal.valueOf(50));

            // Then
            assertTrue(result);
        }

        @Test
        public void test_returns_true_when_balance_is_equal_to_amount() {
            // Given
            Balance balance = Balance.builder().build();
            balance.setFoodBalance(BigDecimal.valueOf(100));

            // When
            boolean result = balance.hasSufficientBalance(BalanceType.FOOD, BigDecimal.valueOf(100));

            // Then
            assertTrue(result);
        }

        @Test
        public void test_returns_false_when_balance_is_less_than_the_amount() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(100)) // set food balance 100
                    .build();

            // When
            boolean result = balance.hasSufficientBalance(BalanceType.FOOD, BigDecimal.valueOf(150)); // Check if balance is sufficient for 150.00

            // Then
            assertFalse(result); // Expecting false as 100.00 is less than 150.00
        }

        @Test
        public void test_handles_null_amount_gracefully() {
            // Given
            Balance balance = new Balance();
            balance.setFoodBalance(BigDecimal.TEN);

            // When
            boolean result = balance.hasSufficientBalance(BalanceType.FOOD, null);

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("debit tests")
    class DebitTests {
        @Test
        public void debit_positive_amount_with_sufficient_funds() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(100))
                    .mealBalance(BigDecimal.valueOf(50))
                    .cashBalance(BigDecimal.valueOf(200))
                    .build();

            // When
            balance.debitAmount(BalanceType.CASH, BigDecimal.valueOf(50));

            // Then
            assertEquals(BigDecimal.valueOf(150), balance.getBalance(BalanceType.CASH));
        }

        @Test
        public void debit_zero_amount() {
            // Given
            Balance balance = Balance.builder()
                    .cashBalance(BigDecimal.valueOf(100))
                    .build();

            // When
            balance.debitAmount(BalanceType.CASH, BigDecimal.ZERO);

            // Then
            assertEquals(BigDecimal.valueOf(100), balance.getBalance(BalanceType.CASH));
        }

        @Test
        public void debit_amount_equal_to_current_balance() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(30))
                    .cashBalance(BigDecimal.valueOf(100))
                    .build();

            // When
            balance.debitAmount(BalanceType.CASH, BigDecimal.valueOf(100));

            // Then
            assertEquals(BigDecimal.ZERO, balance.getBalance(BalanceType.CASH));
        }

        @Test
        public void debit_null_amount() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(30))
                    .cashBalance(BigDecimal.valueOf(100))
                    .build();

            // When
            try {
                balance.debitAmount(BalanceType.CASH, null);
            } catch (IllegalArgumentException e) {
                // Then
                assertEquals("Invalid amount", e.getMessage());
                assertEquals(BigDecimal.valueOf(100), balance.getBalance(BalanceType.CASH));
            }
        }

        @Test
        public void test_debit_negative_amount() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(30))
                    .cashBalance(BigDecimal.valueOf(100))
                    .build();

            // When
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                balance.debitAmount(BalanceType.CASH, BigDecimal.valueOf(-10));
            });

            // Then
            assertEquals("Invalid amount", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), balance.getBalance(BalanceType.CASH));
        }

        @Test
        public void test_debit_amount_does_not_affect_other_balances() {
            // Given
            Balance balance = Balance.builder()
                    .foodBalance(BigDecimal.valueOf(50))
                    .mealBalance(BigDecimal.valueOf(30))
                    .cashBalance(BigDecimal.valueOf(100))
                    .build();

            // When
            balance.debitAmount(BalanceType.CASH, BigDecimal.TEN);

            // Then
            assertEquals(BigDecimal.valueOf(50), balance.getBalance(BalanceType.FOOD));
            assertEquals(BigDecimal.valueOf(30), balance.getBalance(BalanceType.MEAL));
        }

    }

}
