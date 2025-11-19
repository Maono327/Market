package com.maono.marketapplication.unit.services.util;

import com.maono.marketapplication.services.util.DecrementAction;
import com.maono.marketapplication.services.util.DeleteAction;
import com.maono.marketapplication.services.util.IncrementAction;
import com.maono.marketapplication.services.util.ProductActionStrategy;
import com.maono.marketapplication.util.ProductActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProductActionStrategy.class, ProductActionStrategyTest.ProductActionStrategyTestConfiguration.class})
public class ProductActionStrategyTest {

    @TestConfiguration
    public static class ProductActionStrategyTestConfiguration {
        @Bean
        @Primary
        public IncrementAction incrementAction() {
            IncrementAction mock = mock(IncrementAction.class);
            when(mock.getType()).thenReturn(ProductActionType.PLUS);
            return mock;
        }

        @Bean
        @Primary
        public DecrementAction decrementAction() {
            DecrementAction mock = mock(DecrementAction.class);
            when(mock.getType()).thenReturn(ProductActionType.MINUS);
            return mock;
        }

        @Bean
        @Primary
        public DeleteAction deleteAction() {
            DeleteAction mock = mock(DeleteAction.class);
            when(mock.getType()).thenReturn(ProductActionType.DELETE);
            return mock;
        }
    }

    @Autowired
    protected IncrementAction incrementAction;
    @Autowired
    protected DecrementAction decrementAction;
    @Autowired
    protected DeleteAction deleteAction;
    @Autowired
    protected ProductActionStrategy actionStrategy;

    @BeforeEach
    public void setUp() {
        reset(incrementAction, decrementAction, deleteAction);
    }

    @ParameterizedTest
    @EnumSource(value = ProductActionType.class)
    public void test_execute(ProductActionType actionType) {
        switch (actionType) {
            case PLUS -> when(incrementAction.executeChange(anyLong())).thenReturn(Mono.empty());
            case MINUS -> when(decrementAction.executeChange(anyLong())).thenReturn(Mono.empty());
            case DELETE -> when(deleteAction.executeChange(anyLong())).thenReturn(Mono.empty());
        }

        StepVerifier.create(actionStrategy.execute(actionType, 1L))
                .verifyComplete();

        switch (actionType) {
            case PLUS -> {
                verify(incrementAction).executeChange(anyLong());
                verifyNoMoreInteractions(incrementAction);
                verifyNoInteractions(decrementAction, deleteAction);
            }
            case MINUS -> {
                verify(decrementAction).executeChange(anyLong());
                verifyNoMoreInteractions(decrementAction);
                verifyNoInteractions(incrementAction, deleteAction);
            }
            case DELETE -> {
                verify(deleteAction).executeChange(anyLong());
                verifyNoMoreInteractions(deleteAction);
                verifyNoInteractions(incrementAction, decrementAction);
            }
        }
    }
}
