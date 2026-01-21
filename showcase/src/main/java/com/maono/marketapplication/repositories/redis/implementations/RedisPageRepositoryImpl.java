package com.maono.marketapplication.repositories.redis.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.redis.RedisPageRepository;
import com.maono.marketapplication.repositories.redis.util.PageCache;
import com.maono.marketapplication.repositories.util.Page;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Repository
@AllArgsConstructor
public class RedisPageRepositoryImpl implements RedisPageRepository {
    private final ReactiveRedisTemplate<String, PageCache> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String key = "page:";
    private static final Integer TTL = 120;

    @SneakyThrows
    private String getVKey(int pageNumber, int pageSize, String search, String sortType) {
        Map<String, Object> payload = Map.of(
                "pageNumber", pageNumber,
                "pageSize", pageSize,
                "sortType", sortType,
                "search", search
        );

        String jsonKey = objectMapper.writeValueAsString(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(jsonKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Page<Product>> cachePage(Page<Product> page,
                                         int totalPages,
                                         int pageNumber,
                                         int pageSize,
                                         String search,
                                         String sortType) {
        PageCache pageCache = new PageCache(
                page.items(),
                page.pageSize(),
                page.pageNumber(),
                page.hasNext(),
                page.hasPrevious(),
                page.totalPages());

        return redisTemplate
                .opsForValue()
                .set(key + getVKey(pageNumber, pageSize, search, sortType), pageCache, Duration.ofSeconds(TTL))
                .thenReturn(page);
    }

    @Override
    public Mono<PageCache> getCachedPage(String search, int pageSize, int pageNumber, String sortType) {
        return redisTemplate
                .opsForValue()
                .getAndExpire(key + getVKey(pageNumber, pageSize, search, sortType), Duration.ofSeconds(TTL));
    }
}
