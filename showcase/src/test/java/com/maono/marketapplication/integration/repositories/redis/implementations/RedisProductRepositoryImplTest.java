package com.maono.marketapplication.integration.repositories.redis.implementations;

import com.maono.marketapplication.integration.IntegrationTestRedisConfiguration;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.redis.implementations.RedisProductRepositoryImpl;
import com.maono.marketapplication.repositories.redis.util.ProductCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataRedisTest
@Import({
        IntegrationTestRedisConfiguration.class,
        RedisProductRepositoryImpl.class,
})
public class RedisProductRepositoryImplTest {

    @Autowired
    protected RedisProductRepositoryImpl redisProductRepository;
    @Autowired
    protected ReactiveRedisTemplate<String, ProductCache> productRedisTemplate;
    @Autowired
    protected ReactiveRedisTemplate<String, Integer> totalCountRedisTemplate;

    @BeforeEach
    public void clean() {
        productRedisTemplate.execute(connection -> connection.serverCommands()
                .flushAll())
                .then()
                .block();
    }

    @Test
    public void test_cacheObject() {
        Product product = productByIdTemplate(1L).get();

        StepVerifier.create(redisProductRepository.cacheObject(product))
                .assertNext(cached -> assertEquals(productByIdTemplate(1L).get(), cached))
                .verifyComplete();

        StepVerifier.create(productRedisTemplate.opsForValue().get("product:1"))
                        .assertNext(productCache -> {
                            ProductCache expected = new ProductCache(
                                    product.getId(),
                                    product.getTitle(),
                                    product.getDescription(),
                                    product.getImageName(),
                                    product.getPrice()
                            );
                            assertEquals(expected, productCache);
                        })
                        .verifyComplete();

        verifyProductTtl(1L);
    }

    @Test
    public void test_getCachedObject() {
        Product template = productByIdTemplate(1L).get();
        ProductCache productCache = new ProductCache(
                template.getId(),
                template.getTitle(),
                template.getDescription(),
                template.getImageName(),
                template.getPrice()
        );

        productRedisTemplate.opsForValue().set("product:1", productCache, Duration.ofSeconds(120)).block();

        StepVerifier.create(redisProductRepository.getCachedObject(1L))
                .assertNext(product -> assertEquals(productByIdTemplate(1L).get(), product))
                .verifyComplete();

        verifyProductTtl(1L);
    }

    @Test
    public void test_multiGet() {
        List<Product> cached = List.of(
                productByIdTemplate(1L).get(),
                productByIdTemplate(2L).get(),
                productByIdTemplate(4L).get(),
                productByIdTemplate(6L).get(),
                productByIdTemplate(8L).get(),
                productByIdTemplate(9L).get()
        );

        for (Product p : cached) {
            productRedisTemplate.opsForValue()
                    .set("product:" + p.getId(),
                            new ProductCache(
                                    p.getId(),
                                    p.getTitle(),
                                    p.getDescription(),
                                    p.getImageName(),
                                    p.getPrice()
                            )
                    ).block();
        }

        StepVerifier.create(redisProductRepository.multiGet(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)))
                .assertNext(product -> assertEquals(productByIdTemplate(1L).get(), product))
                .assertNext(product -> assertEquals(productByIdTemplate(2L).get(), product))
                .assertNext(product -> assertEquals(productByIdTemplate(4L).get(), product))
                .assertNext(product -> assertEquals(productByIdTemplate(6L).get(), product))
                .assertNext(product -> assertEquals(productByIdTemplate(8L).get(), product))
                .assertNext(product -> assertEquals(productByIdTemplate(9L).get(), product))
                .verifyComplete();

        for (Product p : cached) {
            verifyProductTtl(p.getId());
        }
    }

    @Test
    public void test_cacheTotalCount() {
        StepVerifier.create(redisProductRepository.cacheTotalCount(1, ""))
                .assertNext(count -> assertEquals(1, count))
                .verifyComplete();

        StepVerifier.create(totalCountRedisTemplate.opsForValue().get("total_count:empty"))
                .assertNext(count -> assertEquals(1, count))
                .verifyComplete();

        verifyTotalCountTtl("");
    }

    @Test
    public void test_getCachedTotalCount_search() {
        totalCountRedisTemplate.opsForValue().set("total_count:qwe", 2, Duration.ofSeconds(120)).block();

        StepVerifier.create(redisProductRepository.getCachedTotalCount("qwe"))
                .assertNext(count -> assertEquals(2, count))
                .verifyComplete();

        verifyTotalCountTtl("qwe");
    }


    @Test
    public void test_getCachedTotalCount_empty() {
        totalCountRedisTemplate.opsForValue().set("total_count:empty", 2, Duration.ofSeconds(120)).block();

        StepVerifier.create(redisProductRepository.getCachedTotalCount(""))
                .assertNext(count -> assertEquals(2, count))
                .verifyComplete();

        verifyTotalCountTtl("");
    }

    protected void verifyProductTtl(Long id) {
        StepVerifier.create(productRedisTemplate.getExpire("product:" + id))
                .assertNext(ttl -> {
                    assertTrue(ttl.getSeconds() > 0);
                    assertTrue(ttl.getSeconds() <= 120);
                })
                .verifyComplete();
    }

    protected void verifyTotalCountTtl(String search) {
        StepVerifier.create(totalCountRedisTemplate.getExpire("total_count:" + (search.isBlank() ? "empty" : search)))
                .assertNext(ttl -> {
                    assertTrue(ttl.getSeconds() > 0);
                    assertTrue(ttl.getSeconds() <= 120);
                })
                .verifyComplete();
    }
}
