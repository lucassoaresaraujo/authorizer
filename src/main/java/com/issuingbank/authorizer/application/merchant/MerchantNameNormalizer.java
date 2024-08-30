package com.issuingbank.authorizer.application.merchant;


public class MerchantNameNormalizer {

    /**
     *  Convert to uppercase for consistency
     *  Remove non-alphanumeric characters except spaces
     *  Replace multiple spaces with a single space
     *  Trim leading and trailing whitespace
     * @param merchantName
     * @return normalized name
     */
    public static String normalize(String merchantName) {
        if (merchantName == null) {
            return "";
        }

        return merchantName
                .toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
