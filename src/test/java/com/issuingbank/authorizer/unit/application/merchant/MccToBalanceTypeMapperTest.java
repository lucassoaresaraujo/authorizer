package com.issuingbank.authorizer.unit.application.merchant;

import com.issuingbank.authorizer.application.merchant.MccToBalanceTypeMapper;
import com.issuingbank.authorizer.domain.balance.BalanceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MccToBalanceTypeMapperTest {

    @Test
    public void maps_known_mcc_5411_to_food() {
        // Given
        String mcc = "5411";

        // When
        BalanceType result = MccToBalanceTypeMapper.mapMccToBalanceType(mcc);

        // Then
        assertEquals(BalanceType.FOOD, result);
    }

    @Test
    public void maps_known_mcc_5812_to_meal() {
        // Given
        String mcc = "5812";

        // When
        BalanceType result = MccToBalanceTypeMapper.mapMccToBalanceType(mcc);

        // Then
        assertEquals(BalanceType.MEAL, result);
    }

    @Test
    public void test_returns_cash_for_unknown_mcc() {
        // Given
        String unknownMcc = "9999";

        // When
        BalanceType result = MccToBalanceTypeMapper.mapMccToBalanceType(unknownMcc);

        // Then
        assertEquals(BalanceType.CASH, result);
    }

    @Test
    public void maps_known_mcc_5412_to_food() {
        // Given
        String mcc = "5412";

        // When
        BalanceType result = MccToBalanceTypeMapper.mapMccToBalanceType(mcc);

        // Then
        assertEquals(BalanceType.FOOD, result);
    }

    @Test
    public void test_handles_null_input_gracefully() {
        // Given
        String mcc = null;

        // When
        BalanceType result = MccToBalanceTypeMapper.mapMccToBalanceType(mcc);

        // Then
        assertEquals(BalanceType.CASH, result);
    }
}
