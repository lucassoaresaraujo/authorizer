package com.issuingbank.authorizer.infra.repositories;

import com.issuingbank.authorizer.domain.balance.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Long> {
}