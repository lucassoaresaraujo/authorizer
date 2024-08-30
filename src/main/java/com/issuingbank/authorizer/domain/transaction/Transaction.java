package com.issuingbank.authorizer.domain.transaction;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@Table(name = "transaction")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requested_mcc")
    private String requestedMcc;

    @Column(name = "resolved_mcc")
    private String resolvedMcc;

    @Column(name = "merchant")
    private String merchant;

    @Column(name = "account")
    private String account;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "type")
    private TransactionType type;

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    @Column(name = "created_at")
    private Instant createdAt;


    public boolean isValid() {
        return !requestedMcc.isBlank() && !merchant.isBlank() && amount.compareTo(BigDecimal.ZERO) > 0;
    }

}
