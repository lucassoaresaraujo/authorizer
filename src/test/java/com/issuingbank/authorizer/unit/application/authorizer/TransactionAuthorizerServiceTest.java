package com.issuingbank.authorizer.unit.application.authorizer;

import com.issuingbank.authorizer.application.authorizer.AuthorizationRequest;
import com.issuingbank.authorizer.application.authorizer.AuthorizationResponse;
import com.issuingbank.authorizer.application.authorizer.AuthorizationResponseType;
import com.issuingbank.authorizer.application.authorizer.TransactionAuthorizerService;
import com.issuingbank.authorizer.application.debits.DebitProcessor;
import com.issuingbank.authorizer.application.debits.DebitResult;
import com.issuingbank.authorizer.application.merchant.MccResolverService;
import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceHistory;
import com.issuingbank.authorizer.domain.balance.BalanceType;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import com.issuingbank.authorizer.infra.repositories.BalanceHistoryRepository;
import com.issuingbank.authorizer.infra.repositories.BalanceRepository;
import com.issuingbank.authorizer.infra.repositories.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionAuthorizerServiceTest {
    @InjectMocks
    private TransactionAuthorizerService transactionAuthorizerService;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    @Spy
    private DebitProcessor debitProcessor;

    @Mock
    private MccResolverService mccResolverService;

    @Captor
    private ArgumentCaptor<Balance> balanceCaptor;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Test
    void should_Return_Approved_When_Debit_Is_Successful() {
        // given
        String account = "account";
        BigDecimal requestedAmount = BigDecimal.valueOf(100.0);

        AuthorizationRequest validRequest = new AuthorizationRequest(account, requestedAmount, "1234", "merchant");

        Balance initialBalance = Balance.builder()
                .account(account)
                .foodBalance(BigDecimal.ZERO)
                .mealBalance(BigDecimal.ZERO)
                .cashBalance(BigDecimal.valueOf(200.0))
                .createdAt(Instant.now())
                .build();

        when(balanceRepository.findByAccount(anyString())).thenReturn(Optional.of(initialBalance));
        when(mccResolverService.resolve(anyString(), anyString())).thenReturn("1234");


        UUID idempotencyKey = UUID.randomUUID();

        // when
        AuthorizationResponse response = transactionAuthorizerService.execute(idempotencyKey, validRequest);

        // then
        Assertions.assertEquals(AuthorizationResponseType.APPROVED.getCode(), response.code());
        verify(balanceRepository).save(any(Balance.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));
    }

    @Test
    void should_Return_Insufficient_Balance_When_Debit_Is_Unsuccessful() {
        // given
        String account = "account";
        BigDecimal requestedAmount = BigDecimal.valueOf(100.0);

        AuthorizationRequest validRequest = new AuthorizationRequest(account, requestedAmount, "1234", "merchant");

        Balance initialBalance = Balance.builder()
                .account(account)
                .foodBalance(BigDecimal.ZERO)
                .mealBalance(BigDecimal.ZERO)
                .cashBalance(BigDecimal.valueOf(50.0)) // Saldo insuficiente
                .createdAt(Instant.now())
                .build();

        when(balanceRepository.findByAccount(anyString())).thenReturn(Optional.of(initialBalance));
        when(mccResolverService.resolve(anyString(), anyString())).thenReturn("1234");

        UUID idempotencyKey = UUID.randomUUID();

        // when
        AuthorizationResponse response = transactionAuthorizerService.execute(idempotencyKey, validRequest);

        // then
        Assertions.assertEquals(AuthorizationResponseType.INSUFFICIENT_BALANCE.getCode(), response.code());

        // Verify that the repositories' save methods were not called
        verify(balanceRepository, never()).save(any(Balance.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(balanceHistoryRepository, never()).save(any(BalanceHistory.class));

        verify(debitProcessor).doDebit(eq(initialBalance), eq(BalanceType.CASH), any(Transaction.class));
    }

    @Test
    void should_Return_Approved_When_Food_Balance_Is_Insufficient_But_Cash_Balance_Is_Sufficient() {
        // given
        String account = "account";
        BigDecimal requestedAmount = BigDecimal.valueOf(100.0);

        AuthorizationRequest validRequest = new AuthorizationRequest(account, requestedAmount, "5412", "merchant");

        Balance initialBalance = Balance.builder()
                .account(account)
                .foodBalance(BigDecimal.valueOf(50.0)) // Saldo insuficiente em FOOD
                .mealBalance(BigDecimal.ZERO)
                .cashBalance(BigDecimal.valueOf(200.0)) // Saldo suficiente em CASH
                .createdAt(Instant.now())
                .build();

        Balance updatedBalance = initialBalance.debit(BalanceType.CASH, requestedAmount);
        BigDecimal expectedCashBalance = initialBalance.getCashBalance().subtract(requestedAmount);

        when(balanceRepository.findByAccount(anyString())).thenReturn(Optional.of(initialBalance));
        when(mccResolverService.resolve(anyString(), anyString())).thenReturn("5412");

        UUID idempotencyKey = UUID.randomUUID();

        // when
        AuthorizationResponse response = transactionAuthorizerService.execute(idempotencyKey, validRequest);

        // then
        Assertions.assertEquals(AuthorizationResponseType.APPROVED.getCode(), response.code());


        verify(balanceRepository).save(balanceCaptor.capture()); // Verify that the repositories' save methods were called
        verify(transactionRepository).save(transactionCaptor.capture());
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));


        verify(debitProcessor).doDebit(eq(initialBalance), eq(BalanceType.CASH), any(Transaction.class)); // Capture and assert the DebitResult returned by the DebitProcessor


        Balance capturedBalance = balanceCaptor.getValue(); // Validate the captured Balance
        Assertions.assertEquals(updatedBalance, capturedBalance);

        Assertions.assertEquals(initialBalance.getFoodBalance(), capturedBalance.getFoodBalance()); // Validate that food balance is the same
        Assertions.assertEquals(expectedCashBalance, capturedBalance.getCashBalance());

        verify(debitProcessor).doDebit(eq(initialBalance), eq(BalanceType.FOOD), any(Transaction.class)); // Ensure that doDebit is called for FOOD and CASH
        verify(debitProcessor).doDebit(eq(initialBalance), eq(BalanceType.CASH), any(Transaction.class));
    }
}
