package com.issuingbank.authorizer.e2e;

import com.issuingbank.authorizer.application.authorizer.AuthorizationRequest;
import com.issuingbank.authorizer.application.authorizer.AuthorizationResponseType;
import com.issuingbank.authorizer.application.merchant.CreateMerchantRequest;
import com.issuingbank.authorizer.application.merchant.CreateMerchantService;
import com.issuingbank.authorizer.commons.JsonTransformer;
import com.issuingbank.authorizer.domain.balance.Balance;
import com.issuingbank.authorizer.domain.transaction.Transaction;
import com.issuingbank.authorizer.infra.repositories.BalanceRepository;
import com.issuingbank.authorizer.infra.repositories.TransactionRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthorizerIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @LocalServerPort
    private Integer port;

    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private JsonTransformer jsonTransformer;
    @Autowired
    private CreateMerchantService createMerchantService;
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

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    void connectionEstablished() {
        Assertions.assertTrue(postgres.isCreated());
        Assertions.assertTrue(postgres.isRunning());
    }

    @Test
    void test_should_authorize_when_has_sufficient_balance() {
        // GIVEN
        String accountNumber = "1001";
        String foodMcc = "5412";

        Balance balance = Balance.builder()
                .account(accountNumber)
                .cashBalance(BigDecimal.valueOf(200))
                .mealBalance(BigDecimal.valueOf(750))
                .foodBalance(BigDecimal.valueOf(800))
                .build();
        balanceRepository.save(balance);

        AuthorizationRequest request = AuthorizationRequest.of(
                accountNumber,
                BigDecimal.valueOf(100),
                foodMcc,
                "TEXANO CHURRASCO     TERESINA BR"
        );

        // WHEN
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .body(jsonTransformer.toJson(request))
                .when()
                .post("/authorize")
                .then()
                .extract().response();

        // THEN
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(AuthorizationResponseType.APPROVED.getCode(), response.jsonPath().getString("code"));
    }

    @Test
    void test_authorize_with_insufficient_food_balance_and_sufficient_cash_balance() {
        // GIVEN
        String accountNumber = "1002";
        String foodMcc = "5412";

        Balance balance = Balance.builder()
                .account(accountNumber)
                .cashBalance(BigDecimal.valueOf(200))
                .mealBalance(BigDecimal.valueOf(750))
                .foodBalance(BigDecimal.valueOf(10))
                .build();
        balanceRepository.save(balance);

        AuthorizationRequest request = AuthorizationRequest.of(
                accountNumber,
                BigDecimal.valueOf(100),
                foodMcc,
                "TEXANO CHURRASCO     TERESINA BR"
        );

        // WHEN
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .body(jsonTransformer.toJson(request))
                .when()
                .post("/authorize")
                .then()
                .extract().response();

        // THEN
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(AuthorizationResponseType.APPROVED.getCode(), response.jsonPath().getString("code"));
    }

    @Test
    void test_should_not_authorize_with_insufficient_food_and_cash_balance() {
        // GIVEN
        String accountNumber = "1003";
        String foodMcc = "5412";

        Balance balance = Balance.builder()
                .account(accountNumber)
                .cashBalance(BigDecimal.valueOf(0)) // Sem saldo de cash
                .mealBalance(BigDecimal.valueOf(0)) // Sem saldo de meal
                .foodBalance(BigDecimal.valueOf(10)) // Saldo insuficiente de food
                .build();
        balanceRepository.save(balance);

        AuthorizationRequest request = AuthorizationRequest.of(
                accountNumber,
                BigDecimal.valueOf(100), // Valor da autorização maior que os saldos
                foodMcc,
                "TEXANO CHURRASCO TERESINA BR"
        );

        // WHEN
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .body(jsonTransformer.toJson(request))
                .when()
                .post("/authorize")
                .then()
                .extract().response();

        // THEN
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(AuthorizationResponseType.INSUFFICIENT_BALANCE.getCode(), response.jsonPath().getString("code")); // Código para saldo insuficiente
    }

    @Test
    void test_should_not_authorize_repeated_transaction_with_same_idempotency_key() {
        // GIVEN
        String accountNumber = "1004";
        String foodMcc = "5412";
        String idempotencyKey = UUID.randomUUID().toString(); // Chave de idempotência única

        // Saldo suficiente para a primeira autorização
        Balance balance = Balance.builder()
                .account(accountNumber)
                .cashBalance(BigDecimal.valueOf(300))
                .mealBalance(BigDecimal.valueOf(750))
                .foodBalance(BigDecimal.valueOf(800))
                .build();
        balanceRepository.save(balance);

        AuthorizationRequest request = AuthorizationRequest.of(
                accountNumber,
                BigDecimal.valueOf(100),
                foodMcc,
                "TEXANO CHURRASCO TERESINA BR"
        );

        // WHEN - Primeira requisição
        Response firstResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", idempotencyKey)
                .body(jsonTransformer.toJson(request))
                .when()
                .post("/authorize")
                .then()
                .extract().response();

        // THEN - Verifica se a primeira requisição é autorizada
        Assertions.assertEquals(HttpStatus.OK.value(), firstResponse.getStatusCode());
        Assertions.assertEquals(AuthorizationResponseType.APPROVED.getCode(), firstResponse.jsonPath().getString("code")); // Código de sucesso

        // WHEN - Segunda requisição com a mesma chave de idempotência
        Response secondResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", idempotencyKey) // Usando a mesma chave
                .body(jsonTransformer.toJson(request))
                .when()
                .post("/authorize")
                .then()
                .extract().response();

        // THEN - Verifica se a segunda requisição não é autorizada devido à repetição da chave de idempotência
        Assertions.assertEquals(HttpStatus.OK.value(), secondResponse.getStatusCode());
        Assertions.assertEquals(AuthorizationResponseType.UNEXPECTED_ERROR.getCode(), secondResponse.jsonPath().getString("code")); // Código específico para transação duplicada
    }

    @Test
    void test_should_use_meal_balance_for_known_meal_merchant_even_with_different_mcc() {
        // GIVEN
        String accountNumber = "1005";
        String knownMealMcc = "5812"; // MCC padrão para restaurante/meal
        String anyMcc = "1234"; // MCC qualquer
        String merchantName = "RESTAURANTE SABOROSO TERESINA BR";
        UUID idempotencyKey = UUID.randomUUID();

        // Saldo suficiente de meal, mas insuficiente para outros tipos
        Balance balance = Balance.builder()
                .account(accountNumber)
                .cashBalance(BigDecimal.valueOf(0)) // Sem saldo de cash
                .mealBalance(BigDecimal.valueOf(200)) // Saldo suficiente para meal
                .foodBalance(BigDecimal.valueOf(0)) // Sem saldo de food
                .build();
        balanceRepository.save(balance);

        var merchantRequest = CreateMerchantRequest.create(merchantName, knownMealMcc);
        createMerchantService.execute(merchantRequest);

        // Cria a requisição de autorização
        AuthorizationRequest request = AuthorizationRequest.of(
                accountNumber,
                BigDecimal.valueOf(100), // Valor a ser autorizado
                anyMcc, // MCC diferente do que o merchant está cadastrado
                merchantName // Nome do merchant que já está registrado com MCC de meal
        );

        // WHEN - Faz a requisição de autorização
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", idempotencyKey.toString()) // Usa a chave de idempotência gerada
                .body(jsonTransformer.toJson(request))
                .when()
                .post("/authorize")
                .then()
                .extract().response();

        // THEN - Verifica se a resposta foi autorizada
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(AuthorizationResponseType.APPROVED.getCode(), response.jsonPath().getString("code")); // Código de sucesso

        // Recupera a transação do banco para verificar o tipo de saldo utilizado
        Transaction transaction = transactionRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new AssertionError("Transaction not found"));

        // Verifica se o saldo de meal foi usado na transação
        Assertions.assertEquals(knownMealMcc, transaction.getResolvedMcc());
    }


}
