package com.maono.marketapplication.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.nio.file.Paths;

@Configuration
public class ImageStaticResourceConfig implements WebFluxConfigurer {

    @Value("${showcase.images-dir}")
    private String imagesDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + Paths.get(imagesDir).toAbsolutePath() + "/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }
}