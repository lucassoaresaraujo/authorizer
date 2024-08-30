package com.issuingbank.authorizer.application.merchant;

public record CreateMerchantRequest(
        String name,
        String mcc
) {
    public static CreateMerchantRequest create(String name, String mcc) {
        return new CreateMerchantRequest(name, mcc);
    }
}
