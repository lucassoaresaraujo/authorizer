package com.issuingbank.authorizer.application.authorizer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorizationResponseType {
    APPROVED("00"),
    INSUFFICIENT_BALANCE("51"),
    UNEXPECTED_ERROR("07");

    private final String code;
}
