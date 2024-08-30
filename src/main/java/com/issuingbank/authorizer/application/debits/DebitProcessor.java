package com.issuingbank.authorizer.application.debits;

import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceHistory;
import com.issuingbank.authorizer.domain.balance.BalanceType;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DebitProcessor {

    public Optional<DebitResult> doDebit(Balance currentBalance, BalanceType sourceBalanceType, Transaction debitTransaction) {
        if (currentBalance.hasSufficientBalance(sourceBalanceType, debitTransaction.getAmount())) {
            var newBalance = currentBalance.debit(sourceBalanceType, debitTransaction.getAmount());
            var balanceHistory = BalanceHistory.create(currentBalance, newBalance, debitTransaction);

            return Optional.of(DebitResult.create(newBalance, debitTransaction, balanceHistory));
        }

        return Optional.empty();
    }
}
