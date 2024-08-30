package com.issuingbank.authorizer.application.merchant;

import com.issuingbank.authorizer.domain.balance.BalanceType;

import java.util.HashMap;
import java.util.Map;

public class MccToBalanceTypeMapper {
    private static final Map<String, BalanceType> mccToCategoryMap = new HashMap<>();

    static {
        mccToCategoryMap.put("5411", BalanceType.FOOD);
        mccToCategoryMap.put("5412", BalanceType.FOOD);
        mccToCategoryMap.put("5811", BalanceType.MEAL);
        mccToCategoryMap.put("5812", BalanceType.MEAL);
    }

    public static BalanceType mapMccToBalanceType(String mcc) {
        return mccToCategoryMap.getOrDefault(mcc, BalanceType.CASH);
    }
}
