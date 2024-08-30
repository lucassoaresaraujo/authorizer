package com.issuingbank.authorizer.domain.balance;

import com.issuingbank.authorizer.domain.transaction.Transaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "balance_history")
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account")
    private String account;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "previous_food_balance")
    private BigDecimal previousFoodBalance;

    @Column(name = "previous_meal_balance")
    private BigDecimal previousMealBalance;

    @Column(name = "previous_cash_balance")
    private BigDecimal previousCashBalance;

    @Column(name = "new_food_balance")
    private BigDecimal newFoodBalance;

    @Column(name = "new_meal_balance")
    private BigDecimal newMealBalance;

    @Column(name = "new_cash_balance")
    private BigDecimal newCashBalance;

    @Column(name = "created_at")
    private Instant createdAt;

    public static BalanceHistory create(Balance previousBalance, Balance newBalance, Transaction transaction) {
        return BalanceHistory.builder()
                .transaction(transaction)
                .account(transaction.getAccount())
                .previousMealBalance(previousBalance.getMealBalance())
                .previousFoodBalance(previousBalance.getFoodBalance())
                .previousCashBalance(previousBalance.getCashBalance())
                .newMealBalance(newBalance.getMealBalance())
                .newFoodBalance(newBalance.getFoodBalance())
                .newCashBalance(newBalance.getCashBalance())
                .createdAt(Instant.now())
                .build();
    }
}
