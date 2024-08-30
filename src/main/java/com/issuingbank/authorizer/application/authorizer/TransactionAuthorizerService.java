package com.issuingbank.authorizer.application.authorizer;

import com.issuingbank.authorizer.application.debits.DebitProcessor;
import com.issuingbank.authorizer.application.debits.DebitResult;
import com.issuingbank.authorizer.application.merchant.MccResolverService;
import com.issuingbank.authorizer.application.merchant.MccToBalanceTypeMapper;
import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceType;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import com.issuingbank.authorizer.domain.transaction.TransactionType;
import com.issuingbank.authorizer.infra.repositories.BalanceHistoryRepository;
import com.issuingbank.authorizer.infra.repositories.BalanceRepository;
import com.issuingbank.authorizer.infra.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class TransactionAuthorizerService {
    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final DebitProcessor debitProcessor;
    private final MccResolverService mccResolverService;

    @Transactional()
    public AuthorizationResponse execute(UUID idempotencyKey, AuthorizationRequest request) {
        if (!request.isValid()) {
            return AuthorizationResponse.from(AuthorizationResponseType.UNEXPECTED_ERROR.getCode());
        }

        String resolvedMcc = mccResolverService.resolve(request.mcc(), request.merchant());

        BalanceType primaryBalanceTypeSource = MccToBalanceTypeMapper.mapMccToBalanceType(resolvedMcc);

        var transaction = Transaction.builder()
                .requestedMcc(request.mcc())
                .resolvedMcc(resolvedMcc)
                .merchant(request.merchant())
                .amount(request.totalAmount())
                .account(request.account())
                .type(TransactionType.DEBIT)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .build();

        var currentBalance = balanceRepository.findByAccount(request.account())
                .orElseThrow(() -> new IllegalArgumentException("Account currentBalance not found!"));

        Optional<DebitResult> debitResult = attemptDebitWithFallback(currentBalance, primaryBalanceTypeSource, transaction);

        if (debitResult.isEmpty()) {
            return AuthorizationResponse.from(AuthorizationResponseType.INSUFFICIENT_BALANCE.getCode());
        }

        balanceRepository.save(debitResult.get().newBalance());
        transactionRepository.save(debitResult.get().debitTransaction());
        balanceHistoryRepository.save(debitResult.get().balanceHistory());

        return AuthorizationResponse.from(AuthorizationResponseType.APPROVED.getCode());
    }

    private Optional<DebitResult> attemptDebitWithFallback(Balance currentBalance, BalanceType primaryBalanceTypeSource, Transaction debitTransaction) {
        Optional<DebitResult> result = debitProcessor.doDebit(currentBalance, primaryBalanceTypeSource, debitTransaction);

        if (result.isEmpty() && !primaryBalanceTypeSource.equals(BalanceType.CASH)) {
            result = debitProcessor.doDebit(currentBalance, BalanceType.CASH, debitTransaction);
        }

        return result;
    }
}
