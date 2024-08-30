package com.issuingbank.authorizer.application.authorizer;

public record AuthorizationResponse(String code) {
    public static AuthorizationResponse from(String code) {
        return new AuthorizationResponse(code);
    }
}
