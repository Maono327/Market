package com.maono.marketapplication.controllers;

import com.maono.marketapplication.exceptions.BalanceNotFoundException;
import com.maono.marketapplication.exceptions.PurchaseServiceUnavailabe;
import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.dto.requests.CartPageCountChangeRequest;
import com.maono.marketapplication.models.dto.responses.ProductDto;
import com.maono.marketapplication.models.mappers.ProductDtoMapper;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.implementations.PurchaseServiceImpl;
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
    private final PurchaseServiceImpl purchaseService;

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
                .flatMap(cartItems -> {
                    final List<ProductDto> items = cartItems.stream()
                            .map(CartItem::getProduct)
                            .map(ProductDtoMapper::mapProductToDto)
                            .toList();

                    final BigDecimal totalSum = cartItemService.calculateTotalSum(cartItems);

                    return purchaseService.getBalance()
                            .map(balance -> {
                                boolean isInsufficientFunds = balance.compareTo(totalSum) < 0;

                                Rendering.Builder<?> bBuilder = Rendering.view("cart")
                                        .modelAttribute("items", items)
                                        .modelAttribute("total", totalSum)
                                        .modelAttribute("purchase_is_blocked", isInsufficientFunds);

                                if (isInsufficientFunds) {
                                    bBuilder.modelAttribute("error", "insufficientFunds");
                                }
                                return bBuilder.build();

                            })
                            .onErrorResume(BalanceNotFoundException.class, ex -> Mono.just(
                                    Rendering.view("cart")
                                            .modelAttribute("items", items)
                                            .modelAttribute("total", totalSum)
                                            .modelAttribute("purchase_is_blocked", true)
                                            .modelAttribute("error", "balanceNotFound")
                                            .build()
                            ))
                            .onErrorResume(PurchaseServiceUnavailabe.class, ex -> Mono.just(
                                    Rendering.view("cart")
                                            .modelAttribute("items", items)
                                            .modelAttribute("total", totalSum)
                                            .modelAttribute("purchase_is_blocked", true)
                                            .modelAttribute("error", "unavailable")
                                            .build()
                            ));
                });
    }
}
