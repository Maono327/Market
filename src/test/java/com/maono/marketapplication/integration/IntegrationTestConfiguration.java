package com.maono.marketapplication.integration;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@TestConfiguration
@Import(PostgresqlContainerConfiguration.class)
public class IntegrationTestConfiguration {
    @Bean
    public ResetDataManager resetDataManager(R2dbcEntityTemplate r2dbcEntityTemplate) {
        return new ResetDataManager(r2dbcEntityTemplate);
    }
}
