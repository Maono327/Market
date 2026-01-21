package com.maono.marketapplication.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maono.marketapplication.RedisContainerConfiguration;
import com.maono.marketapplication.configs.RedisConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        RedisConfig.class,
        ObjectMapper.class,
        RedisContainerConfiguration.class
})
public class IntegrationTestRedisConfiguration {
}
