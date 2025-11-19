package com.maono.marketapplication.controllers;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.responses.ProductDto;
import com.maono.marketapplication.models.dto.requests.ProductPageCartCountChangeRequest;
import com.maono.marketapplication.models.dto.requests.ProductsPageCartCountChangeRequest;
import com.maono.marketapplication.models.mappers.PageDtoMapper;
import com.maono.marketapplication.models.mappers.ProductDtoMapper;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(path = {"/", "/items"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CartItemService cartItemService;

    @GetMapping
    public Mono<Rendering> getProducts(@RequestParam(name = "search", defaultValue = "", required = false) String search,
                                       @RequestParam(name = "sort", defaultValue = "NO", required = false) ProductSortType sort,
                                       @RequestParam(name = "pageSize", defaultValue = "5", required = false) int pageSize,
                                       @RequestParam(name = "pageNumber", defaultValue = "1", required = false) int pageNumber) {
        return productService.findByPage(search, sort, pageSize, pageNumber)
                .flatMap(page -> {
                    List<Product> products = page.items();
                    return Flux.fromIterable(products)
                            .collectList()
                            .map(productsWithCartItems -> {
                                List<List<ProductDto>> items = new ArrayList<>();
                                List<ProductDto> row = new ArrayList<>();
                                for (int i = 0;
                                     i < (productsWithCartItems.size() + (productsWithCartItems.size() % 3 == 0 ?
                                             0 : 3 - (productsWithCartItems.size() % 3)));
                                     i++) {
                                    if (i < productsWithCartItems.size()) {
                                        Product product = productsWithCartItems.get(i);
                                        row.add(ProductDtoMapper.mapProductToDto(product));
                                    } else {
                                        row.add(ProductDto.builder().id(-1).build());
                                    }
                                    if (row.size() == 3) {
                                        items.add(row);
                                        row = new ArrayList<>();
                                    }
                                }

                                return Rendering.view("product_items")
                                        .modelAttribute("items", items)
                                        .modelAttribute("search", search)
                                        .modelAttribute("paging", PageDtoMapper.mapToDto(page))
                                        .modelAttribute("sort", sort.toString())
                                        .build();
                            });
                });
    }

    @PostMapping
    public Mono<Rendering> changeProductCountInTheCart(@ModelAttribute ProductsPageCartCountChangeRequest request) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/items");

        uriComponentsBuilder.queryParam("search", request.search());
        uriComponentsBuilder.queryParam("sort", request.sort().toString());
        uriComponentsBuilder.queryParam("pageNumber", request.pageNumber());
        uriComponentsBuilder.queryParam("pageSize", request.pageSize());
        return cartItemService.changeProductCountInTheCart(request.id(), request.action())
                        .then(Mono.just(Rendering.redirectTo(uriComponentsBuilder.toUriString()).build()));
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getProduct(@PathVariable("id") Long id) {
        return productService.findProductByIdWithRelations(id)
                        .map(product -> Rendering.view("product_item")
                                .modelAttribute("item", ProductDtoMapper.mapProductToDto(product))
                                .build());
    }

    @PostMapping("/{id}")
    public Mono<Rendering> changeProductCountInTheCartOnProductPage(@PathVariable("id") Long id,
                                                                    @ModelAttribute ProductPageCartCountChangeRequest request) {

        return cartItemService.changeProductCountInTheCart(id, request.action())
                        .then(productService.findProductByIdWithRelations(id)
                                .map(product -> Rendering.view("product_item")
                                        .modelAttribute("item", ProductDtoMapper.mapProductToDto(product))
                                        .build()));
    }
}
