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
import reactor.core.publisher.Mono;

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

        Product book = Product.builder()
                .title("Книга")
                .description("Интересная книга")
                .imageName("book.png")
                .price(BigDecimal.valueOf(699.99))
                .build();

        Product briefcase = Product.builder()
                .title("Портфель")
                .description("Удобный и красивый портфель")
                .imageName("briefcase.png")
                .price(BigDecimal.valueOf(4000))
                .build();

        Product polaroid = Product.builder()
                .title("Polaroid")
                .description("Для крутых фотографий")
                .imageName("polaroid.png")
                .price(BigDecimal.valueOf(6599.99))
                .build();

        Product umbrella = Product.builder()
                .title("Зонтик")
                .description("Красивый и прозрачный зонт")
                .imageName("umbrella.png")
                .price(BigDecimal.valueOf(1999.99))
                .build();

        Product vase = Product.builder()
                .title("Ваза")
                .description("Дизайнерская ваза")
                .imageName("vase.png")
                .price(BigDecimal.valueOf(12999.99))
                .build();


        Mono<?> zip = Mono.zip(productRepository.findProductByTitleLike(book.getTitle())
                        .switchIfEmpty(productRepository.save(book)
                                .doOnSuccess(p -> log.info("Книга добавлена"))),
                productRepository.findProductByTitleLike(briefcase.getTitle())
                        .switchIfEmpty(productRepository.save(briefcase)
                                .doOnSuccess(p -> log.info("Портфель добавлен"))),
                productRepository.findProductByTitleLike(polaroid.getTitle())
                        .switchIfEmpty(productRepository.save(polaroid)
                                .doOnSuccess(p -> log.info("Полароид добавлен"))),
                productRepository.findProductByTitleLike(umbrella.getTitle())
                        .switchIfEmpty(productRepository.save(umbrella)
                                .doOnSuccess(p -> log.info("Зонтик добавлен"))),
                productRepository.findProductByTitleLike(vase.getTitle())
                        .switchIfEmpty(productRepository.save(vase)
                                .doOnSuccess(p -> log.info("Ваза добавлена")))
                )
                .doOnSuccess(p -> log.info("TEST DATA FILLING IS FINISHED"));

        zip.subscribe();
    }
}
