package com.issuingbank.authorizer.infra.repositories;

import com.issuingbank.authorizer.domain.balance.Balance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "250")}) // timeout in milliseconds to get the lock
    Optional<Balance> findByAccount(String accountId);


    Optional<Balance> findFirstByAccount(String accountId);

}
