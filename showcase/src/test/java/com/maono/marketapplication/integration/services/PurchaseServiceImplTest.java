package com.maono.marketapplication.integration.services;

import com.maono.marketapplication.configs.PaymentServiceClientConfig;
import com.maono.marketapplication.exceptions.BalanceNotFoundException;
import com.maono.marketapplication.exceptions.InsufficientFundsException;
import com.maono.marketapplication.exceptions.PurchaseServiceUnavailabe;
import com.maono.marketapplication.services.implementations.PurchaseServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {PurchaseServiceImpl.class, PaymentServiceClientConfig.class})
class PurchaseServiceImplTest {
    @Autowired
    protected PurchaseServiceImpl purchaseService;

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start(8090);
    }

    @AfterEach
    void clear() throws IOException {
        server.shutdown();
    }

    @Test
    void getBalance_ok_returnsValue() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("123.45"));

        StepVerifier.create(purchaseService.getBalance())
                .expectNext(new BigDecimal("123.45"))
                .verifyComplete();

        RecordedRequest req = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(req);
        assertEquals("GET", req.getMethod());

        assertTrue(req.getPath().startsWith("/balance"));
    }

    @Test
    void getBalance_404_balanceNotFoundException() {
        server.enqueue(new MockResponse().setResponseCode(404));

        StepVerifier.create(purchaseService.getBalance())
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(BalanceNotFoundException.class, ex);
                    assertEquals("Баланс не найден", ex.getMessage());
                })
                .verify();
    }

    @Test
    void getBalance_503_PurchaseServiceUnavailabeException() {
        server.enqueue(new MockResponse().setResponseCode(503));

        StepVerifier.create(purchaseService.getBalance())
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(PurchaseServiceUnavailabe.class, ex);
                    assertEquals("Сервис платежей не доступен", ex.getMessage());
                })
                .verify();
    }

    @Test
    void doPayment_ok() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("10.50")))
                .verifyComplete();

        RecordedRequest req = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(req);
        assertEquals("POST", req.getMethod());

        assertTrue(req.getPath().startsWith("/buy"));
        assertTrue(req.getPath().contains("sum="));
    }

    @Test
    void doPayment_400_illegalArgumentException() {
        server.enqueue(new MockResponse().setResponseCode(400));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("0.00")))
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(IllegalArgumentException.class, ex);
                    assertEquals("Некорректная сумма", ex.getMessage());
                })
                .verify();
    }

    @Test
    void doPayment_409_insufficientFundsException() {
        server.enqueue(new MockResponse().setResponseCode(409));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("999999")))
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(InsufficientFundsException.class, ex);
                    assertEquals("Недостаточно средств", ex.getMessage());
                })
                .verify();
    }
}
