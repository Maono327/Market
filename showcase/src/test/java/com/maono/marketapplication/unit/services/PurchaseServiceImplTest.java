package com.maono.marketapplication.unit.services;

import com.maono.marketapplication.exceptions.BalanceNotFoundException;
import com.maono.marketapplication.exceptions.InsufficientFundsException;
import com.maono.marketapplication.exceptions.PurchaseServiceUnavailabe;
import com.maono.marketapplication.paymentservice.client.api.PaymentApi;
import com.maono.marketapplication.services.implementations.PurchaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {PurchaseServiceImpl.class})
public class PurchaseServiceImplTest {
    @Autowired
    protected PurchaseServiceImpl purchaseService;
    @MockitoBean
    protected PaymentApi paymentApi;

    @Test
    public void test_getBalance_ok() {
        when(paymentApi.balance()).thenReturn(Mono.just(new BigDecimal("100.00")));

        StepVerifier.create(purchaseService.getBalance())
                .assertNext(bigDecimal -> assertEquals(new BigDecimal("100.00"), bigDecimal))
                .verifyComplete();

        verify(paymentApi).balance();
        verifyNoMoreInteractions(paymentApi);
    }

    @Test
    public void test_doPayment_ok() {
        when(paymentApi.buy(any(BigDecimal.class))).thenReturn(Mono.empty());

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("100.00")))
                .expectNextCount(0)
                .verifyComplete();

        verify(paymentApi).buy(eq(new BigDecimal("100.00")));
        verifyNoMoreInteractions(paymentApi);
    }

    @Test
    public void test_getBalance_404() {
        WebClientResponseException exception = new WebClientResponseException(
                HttpStatusCode.valueOf(404).value(),
                "", HttpHeaders.EMPTY,
                new byte[0],
                Charset.defaultCharset());

        when(paymentApi.balance()).thenReturn(Mono.error(exception));

        StepVerifier.create(purchaseService.getBalance())
                .expectError(BalanceNotFoundException.class)
                .verify();

        verify(paymentApi).balance();
        verifyNoMoreInteractions(paymentApi);
    }

    @Test
    void test_getBalance_throwWebClientRequestException() {
        WebClientRequestException exception =
                new WebClientRequestException(
                        new RuntimeException("Connection refused"),
                        HttpMethod.POST,
                        URI.create("http://127.0.0.1:8090/buy"),
                        HttpHeaders.EMPTY
                );

        when(paymentApi.balance()).thenReturn(Mono.error(exception));

        StepVerifier.create(purchaseService.getBalance())
                .expectError(PurchaseServiceUnavailabe.class)
                .verify();

        verify(paymentApi).balance();
        verifyNoMoreInteractions(paymentApi);
    }

    @Test
    public void test_doPayment_400() {
        WebClientResponseException exception = new WebClientResponseException(
                HttpStatusCode.valueOf(400).value(),
                "", HttpHeaders.EMPTY,
                new byte[0],
                Charset.defaultCharset());

        when(paymentApi.buy(any(BigDecimal.class))).thenReturn(Mono.error(exception));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("100.00")))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(paymentApi).buy(eq(new BigDecimal("100.00")));
        verifyNoMoreInteractions(paymentApi);
    }

    @Test
    public void test_doPayment_404() {
        WebClientResponseException exception = new WebClientResponseException(
                HttpStatusCode.valueOf(404).value(),
                "", HttpHeaders.EMPTY,
                new byte[0],
                Charset.defaultCharset());

        when(paymentApi.buy(any(BigDecimal.class))).thenReturn(Mono.error(exception));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("100.00")))
                .expectError(BalanceNotFoundException.class)
                .verify();

        verify(paymentApi).buy(eq(new BigDecimal("100.00")));
        verifyNoMoreInteractions(paymentApi);
    }

    @Test
    public void test_doPayment_409() {
        WebClientResponseException exception = new WebClientResponseException(
                HttpStatusCode.valueOf(409).value(),
                "", HttpHeaders.EMPTY,
                new byte[0],
                Charset.defaultCharset());

        when(paymentApi.buy(any(BigDecimal.class))).thenReturn(Mono.error(exception));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("100.00")))
                .expectError(InsufficientFundsException.class)
                .verify();

        verify(paymentApi).buy(eq(new BigDecimal("100.00")));
        verifyNoMoreInteractions(paymentApi);
    }

    @Test
    public void test_doPayment_503() {
        WebClientResponseException exception = new WebClientResponseException(
                HttpStatusCode.valueOf(503).value(),
                "", HttpHeaders.EMPTY,
                new byte[0],
                Charset.defaultCharset());

        when(paymentApi.buy(any(BigDecimal.class))).thenReturn(Mono.error(exception));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("100.00")))
                .expectError(PurchaseServiceUnavailabe.class)
                .verify();

        verify(paymentApi).buy(eq(new BigDecimal("100.00")));
        verifyNoMoreInteractions(paymentApi);
    }


    @Test
    void test_doPayment_throwWebClientRequestException() {
        WebClientRequestException exception =
                new WebClientRequestException(
                        new RuntimeException("Connection refused"),
                        HttpMethod.POST,
                        URI.create("http://127.0.0.1:8090/buy"),
                        HttpHeaders.EMPTY
                );

        when(paymentApi.buy(eq(new BigDecimal("100.00"))))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(purchaseService.doPayment(new BigDecimal("100.00")))
                .expectError(PurchaseServiceUnavailabe.class)
                .verify();

        verify(paymentApi).buy(eq(new BigDecimal("100.00")));
        verifyNoMoreInteractions(paymentApi);
    }

}
