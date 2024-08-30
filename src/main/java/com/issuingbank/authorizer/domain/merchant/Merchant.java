package com.issuingbank.authorizer.domain.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@Table(name = "merchant")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_merchant_name", nullable = false)
    private String originalMerchantName;

    @Column(name = "normalized_merchant_name", nullable = false)
    private String normalizedMerchantName;

    @Column(name = "corrected_mcc", nullable = false, length = 4)
    private String correctedMcc;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
