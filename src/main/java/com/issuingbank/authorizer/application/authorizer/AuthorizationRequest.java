package com.issuingbank.authorizer.application.authorizer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public record AuthorizationRequest(
        String account,
        BigDecimal totalAmount,
        String mcc,
        String merchant
) {
    @JsonIgnore
    public boolean isValid() {
        return !account.isBlank() && !mcc.isBlank() && !merchant.isBlank()
                && totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static AuthorizationRequest of(String account, BigDecimal totalAmount, String mcc, String merchant) {
        return new AuthorizationRequest(account, totalAmount, mcc, merchant);
    }
}
