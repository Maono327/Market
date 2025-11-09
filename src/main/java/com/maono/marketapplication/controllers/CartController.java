package com.maono.marketapplication.controllers;

import com.maono.marketapplication.models.CartItem;
import com.maono.marketapplication.models.dto.ProductDto;
import com.maono.marketapplication.models.mappers.ProductDtoMapper;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.util.ProductActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart/items")
@RequiredArgsConstructor
public class CartController {

    private final CartItemService cartItemService;

    @GetMapping
    public String getCartItems(Model model) {
        fillModelAttributes(model);
        return "cart";
    }

    @PostMapping
    public String changeProductCountInTheCart(Model model,
                                              @RequestParam("id") Long id,
                                              @RequestParam("action")ProductActionType actionType) {
        cartItemService.changeProductCountInTheCart(id, actionType);

        fillModelAttributes(model);
        return "cart";
    }

    protected void fillModelAttributes(Model model) {
        List<CartItem> cartItems = cartItemService.findAll();

        BigDecimal totalSum = cartItemService.calculateTotalSum(cartItems);

        List<ProductDto> items = cartItems.stream()
                .map(CartItem::getProduct)
                .map(ProductDtoMapper::mapProductToDto)
                .toList();

        model.addAttribute("items", items);
        model.addAttribute("total", totalSum);
    }
}
