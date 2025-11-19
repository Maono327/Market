package com.maono.marketapplication.integration.repositories;

import com.maono.marketapplication.integration.IntegrationTestConfiguration;
import com.maono.marketapplication.integration.ResetDataManager;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.repositories.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedCartItemTestDataProvider.cartItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataR2dbcTest
@Import(IntegrationTestConfiguration.class)
class CartItemRepositoryTest {
    @Autowired
    protected CartItemRepository cartItemRepository;
    @Autowired
    protected ResetDataManager resetDataManager;

    @Test
    public void test_findAll() {
        StepVerifier.create(cartItemRepository.findAll())
                .assertNext(cartItem -> {
                    assertEquals(1L, cartItem.getId());
                    assertEquals(3, cartItem.getCount());
                })
                .assertNext(cartItem -> {
                    assertEquals(2L, cartItem.getId());
                    assertEquals(1, cartItem.getCount());
                })
                .assertNext(cartItem -> {
                    assertEquals(5L, cartItem.getId());
                    assertEquals(2, cartItem.getCount());
                })
                .verifyComplete();
    }

    @MethodSource("arguments_test_findById")
    @ParameterizedTest
    public void test_findById(long id, int count) {
        if (count > 0) {
            StepVerifier.create(cartItemRepository.findById(id))
                    .assertNext(cartItem -> {
                        assertEquals(id, cartItem.getId());
                        assertEquals(count, cartItem.getCount());
                    })
                    .verifyComplete();
        } else {
            StepVerifier.create(cartItemRepository.findById(id))
                    .expectNextCount(0)
                    .verifyComplete();
        }
    }

    public static Stream<Arguments> arguments_test_findById() {
        return Stream.of(
                Arguments.of(1L, 3),
                Arguments.of(2L, 1),
                Arguments.of(5L, 2),
                Arguments.of(6L, -1),
                Arguments.of(7L, -1),
                Arguments.of(8L, -1)
        );
    }

    @Test
    public void test_findAllById() {
        List<Long> ids = List.of(1L, 5L);
        StepVerifier.create(cartItemRepository.findAllById(ids))
                .assertNext(cartItem -> {
                    assertEquals(1L, cartItem.getId());
                    assertEquals(3, cartItem.getCount());
                })
                .assertNext(cartItem -> {
                    assertEquals(5L, cartItem.getId());
                    assertEquals(2, cartItem.getCount());
                })
                .verifyComplete();
    }

    @Test
    public void test_deleteAll() {
        StepVerifier.create(cartItemRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        resetDataManager.resetCartItems();
    }

    @Test
    public void test_delete() {
        CartItem cartItem = cartItem(5L, 2).get();

        StepVerifier.create(cartItemRepository.findById(5L))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.delete(cartItem))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findById(5L))
                .expectNextCount(0)
                .verifyComplete();

        resetDataManager.resetCartItems();
    }

    @Test
    public void test_save() {
        CartItem cartItemToSave = cartItem(3L ,10).get();
        cartItemToSave.setNew(true);


        StepVerifier.create(cartItemRepository.findById(3L))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.save(cartItemToSave))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findById(3L))
                .assertNext(cartItem -> {
                    assertEquals(3L, cartItem.getId());
                    assertEquals(10, cartItem.getCount());
                })
                .verifyComplete();

        resetDataManager.resetCartItems();
    }

}