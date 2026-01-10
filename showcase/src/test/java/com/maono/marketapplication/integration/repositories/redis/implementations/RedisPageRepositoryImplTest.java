package com.maono.marketapplication.integration.repositories.redis.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maono.marketapplication.integration.IntegrationTestRedisConfiguration;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.redis.implementations.RedisPageRepositoryImpl;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.util.Page;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.page;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataRedisTest
@Import({
        IntegrationTestRedisConfiguration.class,
        RedisPageRepositoryImpl.class,
        ObjectMapper.class
})
public class RedisPageRepositoryImplTest {
    @Autowired
    protected RedisPageRepositoryImpl redisPageRepository;
    @Autowired
    protected ReactiveRedisTemplate<String, PageCache> redisTemplate;

    @BeforeEach
    public void clean() {
        redisTemplate.execute(connection -> connection.serverCommands()
                .flushAll())
                .then()
                .block();
    }

    @Test
    public void test_cachePage() {
        Page<Product> page = page(
                List.of(
                            productByIdTemplate(1L).get(),
                            productByIdTemplate(2L).get(),
                            productByIdTemplate(3L).get(),
                            productByIdTemplate(4L).get()
                        ),
                5,
                2,
                true,
                true,
                3
        );

        Page<Product> expected = page(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        StepVerifier.create(redisPageRepository.cachePage(page, 3, 2, 5, "", ""))
                .assertNext(cached -> {
                    assertEquals(expected, cached);
                })
                .verifyComplete();

        StepVerifier.create(redisTemplate.opsForValue().get("page:" + getVKey(2, 5, "", "")))
                .assertNext(cached -> {
                    PageCache expectedPageCache = new PageCache(
                            expected.items(),
                            5,
                            2,
                            true,
                            true,
                            3
                    );

                    assertEquals(expectedPageCache, cached);
                })
                .verifyComplete();

        verifyTtl(2, 5, "", "");
    }

    @Test
    public void test_getCachedPage() {
        PageCache pageCache = new PageCache(
                List.of(
                    productByIdTemplate(1L).get(),
                    productByIdTemplate(2L).get(),
                    productByIdTemplate(3L).get(),
                    productByIdTemplate(4L).get()
                ),
                5,
                2,
                true,
                true,
                3
        );

        redisTemplate.opsForValue()
                .set("page:" + getVKey(2, 5, "", ""),
                        pageCache,
                        Duration.ofSeconds(120))
                .block();

        PageCache expected = new PageCache(
                List.of(
                        productByIdTemplate(1L).get(),
                        productByIdTemplate(2L).get(),
                        productByIdTemplate(3L).get(),
                        productByIdTemplate(4L).get()
                ),
                5,
                2,
                true,
                true,
                3
        );
        StepVerifier.create(redisPageRepository.getCachedPage("", 5, 2, ""))
                .assertNext(page -> assertEquals(expected, page))
                .verifyComplete();

        verifyTtl(2, 5, "", "");
    }

    @SneakyThrows
    private String getVKey(int pageNumber, int pageSize, String search, String sortType) {
        Map<String, Object> payload = Map.of(
                "pageNumber", pageNumber,
                "pageSize", pageSize,
                "sortType", sortType,
                "search", search
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonKey = objectMapper.writeValueAsString(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(jsonKey.getBytes(StandardCharsets.UTF_8));
    }

    protected void verifyTtl(int pageNumber, int pageSize, String search, String sortType) {
        StepVerifier.create(redisTemplate.getExpire("page:" + getVKey(pageNumber, pageSize, search, sortType)))
                .assertNext(ttl -> {
                    assertTrue(ttl.getSeconds() > 0);
                    assertTrue(ttl.getSeconds() <= 120);
                })
                .verifyComplete();
    }
}
