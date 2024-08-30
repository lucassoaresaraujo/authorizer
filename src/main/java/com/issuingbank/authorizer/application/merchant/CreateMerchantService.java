package com.issuingbank.authorizer.application.merchant;

import com.issuingbank.authorizer.domain.merchant.Merchant;
import com.issuingbank.authorizer.infra.repositories.MerchantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CreateMerchantService {
    private final MerchantRepository merchantRepository;

    public Merchant execute(CreateMerchantRequest request) {
        String normalizedMerchantName = MerchantNameNormalizer.normalize(request.name());

        Optional<Merchant> existingMerchant = merchantRepository.findByNormalizedMerchantName(normalizedMerchantName);

        if (existingMerchant.isPresent()) {
            throw new RuntimeException("Merchant already exists");
        }

        Merchant merchant = Merchant.builder()
                .originalMerchantName(request.name())
                .normalizedMerchantName(normalizedMerchantName)
                .correctedMcc(request.mcc())
                .createdAt(Instant.now())
                .build();

        return merchantRepository.save(merchant);
    }
}
