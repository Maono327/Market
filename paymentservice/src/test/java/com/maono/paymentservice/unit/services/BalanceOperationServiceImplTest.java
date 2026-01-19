package com.maono.paymentservice.unit.services;

import com.maono.paymentservice.exceptions.BalanceNotFoundException;
import com.maono.paymentservice.exceptions.InsufficientFundsException;
import com.maono.paymentservice.integration.PostgresqlContainerConfiguration;
import com.maono.paymentservice.model.AccountBalance;
import com.maono.paymentservice.repositories.BalanceRepository;
import com.maono.paymentservice.services.impl.BalanceOperationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BalanceOperationServiceImpl.class)
public class BalanceOperationServiceImplTest {

    @Autowired
    protected BalanceOperationServiceImpl balanceOperationService;
    @MockitoBean
    protected BalanceRepository balanceRepository;

    @Test
    void test_saveBalance() {
        AccountBalance balance = new AccountBalance(1L, new BigDecimal("250.10"));
        when(balanceRepository.save(any(AccountBalance.class))).thenReturn(Mono.just(balance));

        AccountBalance expected = new AccountBalance(1L, new BigDecimal("250.10"));
        StepVerifier.create(balanceOperationService.saveBalance(balance))
                .assertNext(saved -> assertEquals(expected, saved))
                .verifyComplete();

        verify(balanceRepository).save(eq(balance));
        verifyNoMoreInteractions(balanceRepository);
    }

    @Test
    void test_getBalance() {
        AccountBalance balanceFromDb = new AccountBalance(1L, new BigDecimal("250.10"));
        when(balanceRepository.findById(1L)).thenReturn(Mono.just(balanceFromDb));

        AccountBalance expected = new AccountBalance(1L, new BigDecimal("250.10"));
        StepVerifier.create(balanceOperationService.getBalance())
                .assertNext(balance -> assertEquals(expected, balance))
                .verifyComplete();

        verify(balanceRepository).findById(1L);
        verifyNoMoreInteractions(balanceRepository);
    }

    @Test
    void test_doPayment() {
        AccountBalance balanceFromDb = new AccountBalance(1L, new BigDecimal("250.10"));
        when(balanceRepository.findById(1L)).thenReturn(Mono.just(balanceFromDb));

        when(balanceRepository.save(any(AccountBalance.class)))
                .thenAnswer(invocationOnMock -> Mono.just(invocationOnMock.getArgument(0)));


        AccountBalance expected = new AccountBalance(1L, new BigDecimal("150.10"));
        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("100")))
                .assertNext(balance -> assertEquals(expected, balance))
                .verifyComplete();

        verify(balanceRepository).findById(1L);
        verify(balanceRepository).save(eq(new AccountBalance(1L, new BigDecimal("150.10"))));
        verifyNoMoreInteractions(balanceRepository);
    }

    @Test
    void test_doPayment_throwIllegalArgumentException_badValue() {
        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("-1")))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(balanceRepository);
    }

    @Test
    void test_doPayment_throwIllegalArgumentException_null() {
        StepVerifier.create(balanceOperationService.doPayment(null))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(balanceRepository);
    }


    @Test
    void test_doPayment_throwBalanceNotFoundException() {
        when(balanceRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("100")))
                .expectError(BalanceNotFoundException.class)
                .verify();

        verify(balanceRepository).findById(1L);
        verifyNoMoreInteractions(balanceRepository);
    }

    @Test
    void test_doPayment_throwInsufficientFundsException() {
        AccountBalance balanceFromDb = new AccountBalance(1L, new BigDecimal("250.10"));
        when(balanceRepository.findById(1L)).thenReturn(Mono.just(balanceFromDb));

        StepVerifier.create(balanceOperationService.doPayment(new BigDecimal("400")))
                .expectError(InsufficientFundsException.class)
                .verify();

        verify(balanceRepository).findById(1L);
        verifyNoMoreInteractions(balanceRepository);
    }
}
