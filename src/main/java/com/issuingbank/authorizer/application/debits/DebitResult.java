package com.issuingbank.authorizer.application.debits;

import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceHistory;
import com.issuingbank.authorizer.domain.transaction.Transaction;

public record DebitResult(
    Balance newBalance,
    Transaction debitTransaction,
    BalanceHistory balanceHistory
) {
    public static DebitResult create(Balance updatedBalance, Transaction debitTransaction, BalanceHistory balanceHistory) {
        return new DebitResult(updatedBalance, debitTransaction, balanceHistory);
    }
}
