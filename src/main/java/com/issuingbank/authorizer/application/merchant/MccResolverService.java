package com.issuingbank.authorizer.application.merchant;

import com.issuingbank.authorizer.domain.merchant.Merchant;
import com.issuingbank.authorizer.infra.repositories.MerchantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class MccResolverService {
    private final MerchantRepository merchantRepository;

    public String resolve(String mcc, String merchantName) {
        String normalizedMerchantName = MerchantNameNormalizer.normalize(merchantName);

        Optional<String> correctedMcc = merchantRepository
                .findByNormalizedMerchantName(normalizedMerchantName)
                .map(Merchant::getCorrectedMcc);

        return correctedMcc.orElse(mcc);
    }
}
