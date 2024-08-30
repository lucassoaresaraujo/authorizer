package com.issuingbank.authorizer.unit.application.merchant;

import com.issuingbank.authorizer.application.merchant.MerchantNameNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MerchantNormalizeTest {
    @Test
    public void teste_normalize_merchant_name() {
        // Given
        String input = "PADARIA DO ZE               SAO PAULO BR";
        String expected = "PADARIA DO ZE SAO PAULO BR";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals(expected, result);
    }

    @Test
    public void converts_lowercase_to_uppercase() {
        // Given
        String input = "merchant";
        String expected = "MERCHANT";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals(expected, result);
    }

    @Test
    public void removes_non_alphanumeric_characters() {
        // Given
        String input = "merchant@name!";
        String expected = "MERCHANTNAME";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals(expected, result);
    }

    @Test
    public void replaces_multiple_spaces_with_single_space() {
        // Given
        String input = "merchant   name";
        String expected = "MERCHANT NAME";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals(expected, result);
    }

    @Test
    public void input_with_only_special_characters() {
        // Given
        String input = "@#$%^&*()";
        String expected = "";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals(expected, result);
    }

    // Input string with multiple consecutive spaces
    @Test
    public void input_with_multiple_consecutive_spaces() {
        // Given
        String input = "merchant     name";
        String expected = "MERCHANT NAME";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals(expected, result);
    }

    @Test
    public void input_with_leading_and_trailing_spaces() {
        // Given
        String input = "  merchant name  ";
        String expected = "MERCHANT NAME";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals(expected, result);
    }

    @Test
    public void test_returns_empty_string_for_null_input() {
        // Given
        String nullMerchantName = null;

        // When
        String result = MerchantNameNormalizer.normalize(nullMerchantName);

        // Then
        assertEquals("", result);
    }

    @Test
    public void test_trims_whitespace() {
        // Given
        String input = "   example   ";

        // When
        String result = MerchantNameNormalizer.normalize(input);

        // Then
        assertEquals("EXAMPLE", result);
    }
}
