package com.issuingbank.authorizer.domain.balance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Map.entry;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@Table(name = "balance")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Balance {

    private static final Map<BalanceType, Function<Balance, BigDecimal>> balanceGetterMap = Map.ofEntries(
            entry(BalanceType.FOOD, Balance::getFoodBalance),
            entry(BalanceType.MEAL, Balance::getMealBalance),
            entry(BalanceType.CASH, Balance::getCashBalance)
    );

    private static final Map<BalanceType, BiConsumer<Balance, BigDecimal>> balanceSetterMap = Map.ofEntries(
            entry(BalanceType.FOOD, Balance::setFoodBalance),
            entry(BalanceType.MEAL, Balance::setMealBalance),
            entry(BalanceType.CASH, Balance::setCashBalance)
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account")
    private String account;

    @Column(name = "food_balance")
    private BigDecimal foodBalance;

    @Column(name = "meal_balance")
    private BigDecimal mealBalance;

    @Column(name = "cash_balance")
    private BigDecimal cashBalance;

    @Version
    private int version;

    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public BigDecimal getBalance(BalanceType balanceType) {
        return balanceGetterMap.get(balanceType).apply(this);
    }

    public void setBalance(BalanceType type, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid amount!");
        }

        balanceSetterMap.get(type).accept(this, amount);
    }

    public boolean hasSufficientBalance(BalanceType balanceType, BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        return this.getBalance(balanceType).compareTo(amount) >= 0;
    }

    public Balance debit(BalanceType balanceType, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        Balance newBalance = new Balance(this.id, this.account, this.foodBalance, this.mealBalance,
                this.cashBalance, this.version, this.createdAt, this.updatedAt);

        newBalance.setBalance(balanceType, getBalance(balanceType).subtract(amount));

        return newBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Balance that = (Balance) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
