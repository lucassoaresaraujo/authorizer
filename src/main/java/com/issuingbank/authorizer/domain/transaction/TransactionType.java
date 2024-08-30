package com.issuingbank.authorizer.domain.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TransactionType {
    CREDIT(1),
    DEBIT(2);

    private final Integer id;

    public static TransactionType getById(Integer id) {
        return Arrays.stream(TransactionType.values())
                .filter(type -> type.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
