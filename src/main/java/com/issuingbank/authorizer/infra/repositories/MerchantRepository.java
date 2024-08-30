package com.issuingbank.authorizer.infra.repositories;

import com.issuingbank.authorizer.domain.merchant.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByNormalizedMerchantName(String normalizedMerchantName);
}
