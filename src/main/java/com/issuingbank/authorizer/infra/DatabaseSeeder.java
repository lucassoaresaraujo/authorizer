package com.issuingbank.authorizer.infra;

import com.issuingbank.authorizer.application.merchant.CreateMerchantRequest;
import com.issuingbank.authorizer.application.merchant.CreateMerchantService;
import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.balance.BalanceHistory;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import com.issuingbank.authorizer.domain.transaction.TransactionType;
import com.issuingbank.authorizer.infra.repositories.BalanceHistoryRepository;
import com.issuingbank.authorizer.infra.repositories.BalanceRepository;
import com.issuingbank.authorizer.infra.repositories.MerchantRepository;
import com.issuingbank.authorizer.infra.repositories.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
public class DatabaseSeeder {
    private final CreateMerchantService createMerchantService;
    private final MerchantRepository merchantRepository;
    private final BalanceRepository balanceRepository;

    @PostConstruct
    @Transactional
    public void initDatabase() {
        populateMerchants();
        createAccounts();
    }

    private void createAccounts() {
        if (balanceRepository.findAll().isEmpty()) {
            /**
             * Poderia ser criado todo um fluxo de criação de account. Onde geraria o registro do balance inicial,
             * suas transactions para adicionar o saldo e juntamente com os registros de balance history para manter toda
             * a rastreabilidade do credito e débito na conta.
             *
             * Aqui está simplificado ...
             */

            Balance balance = Balance.builder()
                    .account("1")
                    .foodBalance(BigDecimal.valueOf(200))
                    .mealBalance(BigDecimal.valueOf(200))
                    .cashBalance(BigDecimal.valueOf(200))
                    .build();

            balanceRepository.save(balance);
        }
    }

    private void populateMerchants() {
        if (merchantRepository.findAll().isEmpty()) {
            createMerchantService.execute(CreateMerchantRequest.create("UBER TRIP                   SAO PAULO BR", "1520"));
            createMerchantService.execute(CreateMerchantRequest.create("UBER EATS                   SAO PAULO BR", "5811"));
        }
    }

}
