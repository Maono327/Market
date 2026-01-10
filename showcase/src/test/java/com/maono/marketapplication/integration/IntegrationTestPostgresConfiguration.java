package com.maono.marketapplication.integration;

import com.maono.marketapplication.PostgresqlContainerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@Import({PostgresqlContainerConfiguration.class})
public class IntegrationTestPostgresConfiguration {
    @Bean
    public ResetDataManager resetDataManager(R2dbcEntityTemplate r2dbcEntityTemplate) {
        return new ResetDataManager(r2dbcEntityTemplate);
    }
}
