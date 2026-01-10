package com.maono.marketapplication.integration;

import com.maono.marketapplication.RedisContainerConfiguration;
import com.maono.marketapplication.configs.RedisConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        RedisConfig.class,
        RedisContainerConfiguration.class
})
public class IntegrationTestRedisConfiguration {
}
