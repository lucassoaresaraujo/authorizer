package com.issuingbank.authorizer.infra.repositories;

import com.issuingbank.authorizer.domain.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(UUID idempotencyKey);

    List<Transaction> findAllByAccount(String account);
}
