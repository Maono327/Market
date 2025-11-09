package com.maono.marketapplication.controllers;

import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.ProductDto;
import com.maono.marketapplication.models.dto.ProductsPageParametersDto;
import com.maono.marketapplication.models.mappers.ProductDtoMapper;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductActionType;
import com.maono.marketapplication.util.ProductSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(path = {"/", "/items"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CartItemService cartItemService;

    @GetMapping
    public String getProducts(Model model,
                              @RequestParam(name = "search", defaultValue = "", required = false) String search,
                              @RequestParam(name = "sort", defaultValue = "NO", required = false) ProductSortType sort,
                              @RequestParam(name = "pageSize", defaultValue = "5", required = false) int pageSize,
                              @RequestParam(name = "pageNumber", defaultValue = "1", required = false) int pageNumber) {

        List<List<ProductDto>> items = new ArrayList<>();
        Page<Product> productsPage = productService.findByPage(search, sort, pageSize, pageNumber - 1);
        List<Product> products = productsPage.toList();

        List<ProductDto> row = new ArrayList<>();
        for (int i = 0; i < (products.size() + (products.size() % 3 == 0 ? 0 : 3 - (products.size() % 3))); i++) {
            if (i < products.size()) {
                Product product = products.get(i);
                row.add(ProductDtoMapper.mapProductToDto(product));
            } else {
                row.add(ProductDto.builder().id(-1).build());
            }
            if (row.size() == 3) {
                items.add(row);
                row = new ArrayList<>();
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("search", search);
        model.addAttribute("paging",
                new ProductsPageParametersDto(
                        productsPage.getSize(),
                        productsPage.getNumber() + 1,
                        productsPage.hasNext(),
                        productsPage.hasPrevious()));
        model.addAttribute("sort", sort.toString());
        return "product_items";
    }

    @PostMapping
    public String changeProductCountInTheCart(RedirectAttributes redirectAttributes,
                                              @RequestParam(name = "id") Long id,
                                              @RequestParam(name = "search", defaultValue = "", required = false) String search,
                                              @RequestParam(name = "sort", defaultValue = "NO", required = false) ProductSortType sort,
                                              @RequestParam(name = "pageSize", defaultValue = "5", required = false) int pageSize,
                                              @RequestParam(name = "pageNumber", defaultValue = "1", required = false) int pageNumber,
                                              @RequestParam(name = "action") ProductActionType actionType) {

        cartItemService.changeProductCountInTheCart(id, actionType);

        redirectAttributes.addAttribute("search", search);
        redirectAttributes.addAttribute("sort", sort.toString());
        redirectAttributes.addAttribute("pageSize", pageSize);
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        return "redirect:/items";
    }

    @GetMapping("/{id}")
    public String getProduct(Model model,
                             @PathVariable("id") Long id) {

        model.addAttribute("item", ProductDtoMapper.mapProductToDto(productService.findProductById(id)));
        return "product_item";
    }

    @PostMapping("/{id}")
    public String changeProductCountInTheCartOnProductPage(Model model,
                                                           @PathVariable("id") Long id,
                                                           @RequestParam("action") ProductActionType actionType) {

        cartItemService.changeProductCountInTheCart(id, actionType);
        Product product = productService.findProductById(id);
        model.addAttribute("item", ProductDtoMapper.mapProductToDto(product));

        return "product_item";
    }
}
