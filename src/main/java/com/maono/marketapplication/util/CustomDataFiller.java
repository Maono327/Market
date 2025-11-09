package com.maono.marketapplication.util;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("TEST_DATA")
@RequiredArgsConstructor
public class CustomDataFiller implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CustomDataFiller.class);
    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("TEST DATA FILLING IS STARTED");

        if (productRepository.findProductByTitle("Книга") == null) {
            Product book = Product.builder()
                    .title("Книга")
                    .description("Интересная книга")
                    .imageName("book.png")
                    .price(BigDecimal.valueOf(699.99))
                    .build();
            productRepository.save(book);
            log.warn("BOOK ADDED");
        }

        if (productRepository.findProductByTitle("Портфель") == null) {
            Product briefcase = Product.builder()
                    .title("Портфель")
                    .description("Удобный и красивый портфель")
                    .imageName("briefcase.png")
                    .price(BigDecimal.valueOf(4000))
                    .build();
            productRepository.save(briefcase);
            log.warn("BRIEFCASE ADDED");
        }

        if (productRepository.findProductByTitle("Polaroid") == null) {
            Product polaroid = Product.builder()
                    .title("Polaroid")
                    .description("Для крутых фотографий")
                    .imageName("polaroid.png")
                    .price(BigDecimal.valueOf(6599.99))
                    .build();
            productRepository.save(polaroid);
            log.warn("POLAROID ADDED");
        }

        if (productRepository.findProductByTitle("Зонтик") == null) {
            Product umbrella = Product.builder()
                    .title("Зонтик")
                    .description("Красивый и прозрачный зонт")
                    .imageName("umbrella.png")
                    .price(BigDecimal.valueOf(1999.99))
                    .build();
            productRepository.save(umbrella);
            log.warn("UMBRELLA ADDED");
        }

        if (productRepository.findProductByTitle("Ваза") == null) {
            Product vase = Product.builder()
                    .title("Ваза")
                    .description("Дизайнерская ваза")
                    .imageName("vase.png")
                    .price(BigDecimal.valueOf(12999.99))
                    .build();
            productRepository.save(vase);
            log.warn("VASE ADDED");
        }


        log.info("TEST DATA FILLING IS FINISHED");
    }
}
