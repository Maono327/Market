package com.maono.marketapplication.configs;

import com.maono.marketapplication.paymentservice.client.api.PaymentApi;
import com.maono.marketapplication.paymentservice.client.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentServiceClientConfig {
    @Bean
    public WebClient paymentServiceWebClient() {
        return WebClient.create();
    }

    @Bean
    public ApiClient paymentServiceApiClient(@Value("${paymentservice.url}") String baseUrl, WebClient paymentServiceWebClient) {
        ApiClient apiClient = new ApiClient(paymentServiceWebClient);
        apiClient.setBasePath(baseUrl);
        return apiClient;
    }

    @Bean
    public PaymentApi paymentApi(ApiClient paymentServiceApiClient) {
        return new PaymentApi(paymentServiceApiClient);
    }
}
