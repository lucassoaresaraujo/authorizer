package com.issuingbank.authorizer.integration;

import com.issuingbank.authorizer.application.authorizer.AuthorizationRequest;
import com.issuingbank.authorizer.application.authorizer.TransactionAuthorizerService;
import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import com.issuingbank.authorizer.infra.repositories.BalanceRepository;
import com.issuingbank.authorizer.infra.repositories.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Slf4j
@SpringBootTest
public class SpringBootIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private TransactionAuthorizerService transactionAuthorizerService;
    @Autowired
    private TransactionRepository transactionRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }


    @Test
    public void test_concurrent_authorizations_with_insufficient_balance() throws InterruptedException {
        // GIVEN
        String accountNumber = "1006";
        String foodMcc = "5411"; // MCC conhecido para food (por exemplo)
        BigDecimal transactionAmount = BigDecimal.valueOf(100); // Valor de cada transação
        int totalAttempts = 10; // Total de transações a serem solicitadas
        int expectedAuthorizedTransactions = 5; // Saldo suficiente para apenas 5 transações

        // Cria a conta com saldo de food suficiente para 5 transações
        BigDecimal foodBalanceAmount = BigDecimal.valueOf(500);

        Balance balance = Balance.builder()
                .account(accountNumber)
                .cashBalance(BigDecimal.ZERO)
                .mealBalance(BigDecimal.ZERO)
                .foodBalance(foodBalanceAmount) // Saldo suficiente para 5 transações de 100
                .build();
        balanceRepository.save(balance);

        doSyncAndConcurrently(totalAttempts, s -> {
            transactionAuthorizerService.execute(UUID.randomUUID(), AuthorizationRequest.of(
                    accountNumber,
                    transactionAmount,
                    foodMcc,
                    "FOOD MERCHANT"
            ));
        });

        // THEN
        List<Transaction> approvedTransactions = transactionRepository.findAllByAccount(accountNumber);
        Balance newBalance = balanceRepository.findFirstByAccount(accountNumber).orElseThrow();
        BigDecimal sumOfTransactionsAmounts = approvedTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        Assertions.assertEquals(expectedAuthorizedTransactions, (long) approvedTransactions.size());
        Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(newBalance.getFoodBalance()));
        Assertions.assertEquals(0, foodBalanceAmount.compareTo(sumOfTransactionsAmounts));
    }

    /**
     * Starts many threads concurrently to execute the <code>operation</code> at the same time.
     * This method only returns after all threads have been executed.
     */
    protected void doSyncAndConcurrently(int threadCount, Consumer<String> operation) throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            String threadName = "Thread-" + i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    operation.accept(threadName);
                } catch (Exception e) {
                    log.error("error while executing operation {}: {}", threadName, e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await();
    }
}
