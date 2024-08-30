package com.issuingbank.authorizer.unit.application.merchant;

import com.issuingbank.authorizer.application.merchant.MccResolverService;
import com.issuingbank.authorizer.application.merchant.MerchantNameNormalizer;
import com.issuingbank.authorizer.domain.merchant.Merchant;
import com.issuingbank.authorizer.infra.repositories.MerchantRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MccResolverServiceTest {

    @InjectMocks
    private MccResolverService mccResolverService;

    @Mock
    MerchantRepository merchantRepository;


    // Normalizing a valid merchant name and finding a corrected MCC
    @Test
    public void test_normalizing_valid_merchant_name_and_finding_corrected_mcc() {
        // Given
        String mcc = "1234";
        String merchantName = "Valid Merchant";
        String normalizedMerchantName = MerchantNameNormalizer.normalize(merchantName);
        String correctedMcc = "5678";

        Mockito.when(merchantRepository.findByNormalizedMerchantName(normalizedMerchantName))
                .thenReturn(Optional.of(Merchant.builder()
                                .originalMerchantName(merchantName)
                                .normalizedMerchantName(normalizedMerchantName)
                                .correctedMcc(correctedMcc)
                        .build()));

        // When
        String result = mccResolverService.resolve(mcc, merchantName);

        // Then
        Assertions.assertEquals(correctedMcc, result);
    }

    // Returning the original MCC when no corrected MCC is found
    @Test
    public void test_returning_original_mcc_when_no_corrected_mcc_found() {
        // Given
        String originalMcc = "1234";
        String merchantName = "Unknown Merchant";
        String normalizedMerchantName = MerchantNameNormalizer.normalize(merchantName);

        Mockito.when(merchantRepository.findByNormalizedMerchantName(normalizedMerchantName))
                .thenReturn(Optional.empty());

        // When
        String result = mccResolverService.resolve(originalMcc, merchantName);

        // Then
        Assertions.assertEquals(originalMcc, result);
    }

    // Handling merchant names with only special characters
    @Test
    public void test_handling_merchant_names_with_special_characters() {
        // Given
        String mcc = "1234";
        String merchantName = "MERCHANT*  SP";
        String normalizedMerchantName = MerchantNameNormalizer.normalize(merchantName);

        Mockito.when(merchantRepository.findByNormalizedMerchantName(normalizedMerchantName))
                .thenReturn(Optional.empty());

        // When
        String result = mccResolverService.resolve(mcc, merchantName);

        // Then

        Mockito.verify(merchantRepository).findByNormalizedMerchantName(normalizedMerchantName);
        Assertions.assertEquals(mcc, result);
    }

}