package com.maono.marketapplication.controllers;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.dto.requests.CartPageCountChangeRequest;
import com.maono.marketapplication.models.dto.responses.ProductDto;
import com.maono.marketapplication.models.mappers.ProductDtoMapper;
import com.maono.marketapplication.services.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {

    private final CartItemService cartItemService;

    @GetMapping
    public Mono<Rendering> getCartItems() {
        return fillModelAttributes();
    }

    @PostMapping
    public Mono<Rendering> changeProductCountInTheCart(@ModelAttribute CartPageCountChangeRequest request) {
        return cartItemService.changeProductCountInTheCart(request.id(), request.action())
                .then(fillModelAttributes());
    }

    protected Mono<Rendering> fillModelAttributes() {
        return cartItemService.findAllWithRelations()
                .collectList()
                .map(cartItems -> {
                    BigDecimal totalSum = cartItemService.calculateTotalSum(cartItems);
                    List<ProductDto> items = cartItems.stream()
                                .map(CartItem::getProduct)
                                .map(ProductDtoMapper::mapProductToDto)
                                .toList();
                    return Rendering.view("cart")
                            .modelAttribute("items", items)
                            .modelAttribute("total", totalSum)
                            .build();
                });
    }
}
