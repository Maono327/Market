package com.maono.marketapplication.unit.controllers;

import com.maono.marketapplication.controllers.ProductController;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.ProductDto;
import com.maono.marketapplication.models.dto.ProductsPageParametersDto;
import com.maono.marketapplication.services.CartItemService;
import com.maono.marketapplication.services.ProductService;
import com.maono.marketapplication.util.ProductActionType;
import com.maono.marketapplication.util.ProductSortType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Pair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductByIdWithCartItem;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductDtoList;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductsList;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.getPage;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.getPageRequest;
import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.mapToProductDto;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(classes = {ProductController.class, ProductService.class})
@AutoConfigureMockMvc
class ProductControllerTest {
    @MockitoBean
    protected ProductService productService;
    @MockitoBean
    protected CartItemService cartItemService;
    @Autowired
    protected MockMvc mockMvc;

    @MethodSource("arguments_test_getProducts")
    @ParameterizedTest
    public void test_getProducts(String search,
                                 String sort,
                                 String pageSize,
                                 String pageNumber,
                                 List<Product> productsFromDb,
                                 List<List<ProductDto>> expectedProductsDtos) throws Exception {
        String searchParam = search == null ? "" : search;
        ProductSortType sortParam = sort == null ? ProductSortType.NO : ProductSortType.valueOf(sort);
        int pageSizeParam = pageSize == null ? 5 : Integer.parseInt(pageSize);
        int pageNumberParam = pageNumber == null ? 1 : Integer.parseInt(pageNumber);

        Page<Product> pageFromService = getPage(
                getPageRequest(pageSizeParam, pageNumberParam, sortParam), productsFromDb);
        when(productService.findByPage(searchParam, sortParam, pageSizeParam, pageNumberParam - 1))
                .thenReturn(pageFromService);

        Map<String,String> params = new HashMap<>();
        if (search != null) params.put("search", search);
        if (sort != null) params.put("sort", sort);
        if (pageSize != null) params.put("pageSize", pageSize);
        if (pageNumber != null) params.put("pageNumber", pageNumber);
        mockMvc.perform(getRequestBuilderByParameters(get("/items"), params))
                .andExpect(view().name("product_items"))
                .andExpect(model().attribute("items", expectedProductsDtos))
                .andExpect(model().attribute("search", searchParam))
                .andExpect(model().attribute("paging", new ProductsPageParametersDto(pageFromService.getSize(),
                        pageFromService.getNumber() + 1,
                        pageFromService.hasNext(),
                        pageFromService.hasPrevious())))
                .andExpect(model().attribute("sort", sortParam.toString()))
                .andExpect(status().isOk());

        verify(productService).findByPage(searchParam, sortParam, pageSizeParam, pageNumberParam - 1);
        verifyNoMoreInteractions(productService);
        verifyNoInteractions(cartItemService);
    }

    protected RequestBuilder getRequestBuilderByParameters(MockHttpServletRequestBuilder requestBuilder,
                                                           Map<String, String> parameters) {
        for (Map.Entry<String, String> p : parameters.entrySet()) {
            requestBuilder.param(p.getKey(), p.getValue());
        }
        return requestBuilder;
    }

    protected static Stream<Arguments> arguments_test_getProducts() {
        return Stream.of(
                Arguments.of(null, null, null, null,
                        buildProductsList(1L, 2L, 3L),
                        List.of(buildProductDtoList(Pair.of(1L, 0), Pair.of(2L, 0), Pair.of(3L, 0)))),
                Arguments.of(null, "ALPHA", "2", "1",
                        buildProductsList(3L, 5L),
                        List.of(buildProductDtoList(Pair.of(3L, 0), Pair.of(5L, 0), Pair.of(-1L, 0)))),
                Arguments.of("о", null, null, null,
                        buildProductsList(Pair.of(2L, 1), Pair.of(4L, 2)),
                        List.of(buildProductDtoList(Pair.of(2L, 1), Pair.of(4L, 2), Pair.of(-1L, 0))))
        );
    }

    @MethodSource("arguments_test_changeProductCountInTheCart")
    @ParameterizedTest
    public void test_changeProductCountInTheCart(String id,
                                                 String search,
                                                 String sort,
                                                 String pageSize,
                                                 String pageNumber,
                                                 String action) throws Exception {
        Long idParam = Long.parseLong(id);
        ProductActionType actionParam = ProductActionType.valueOf(action);

        doNothing().when(cartItemService).changeProductCountInTheCart(idParam, actionParam);

        Map<String,String> params = new HashMap<>();
        if (search != null) params.put("search", search);
        if (sort != null) params.put("sort", sort);
        if (pageSize != null) params.put("pageSize", pageSize);
        if (pageNumber != null) params.put("pageNumber", pageNumber);
        mockMvc.perform(getRequestBuilderByParameters(post("/items")
                                .param("id", id).param("action", action), params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items?search=*&sort=*&pageSize=*&pageNumber=*"));

        verify(cartItemService).changeProductCountInTheCart(idParam, actionParam);
        verifyNoMoreInteractions(cartItemService);
        verifyNoInteractions(productService);
    }

    protected static Stream<Arguments> arguments_test_changeProductCountInTheCart() {
        return Stream.of(
                Arguments.of("1", null, null, null, null, "PLUS"),
                Arguments.of("2", null, "ALPHA", null, null, "MINUS"),
                Arguments.of("3", null, null, "5", null, "PLUS"),
                Arguments.of("3", "Книга", null, null, null, "PLUS"),
                Arguments.of("3", "Книга", null, "5", null, "PLUS"),
                Arguments.of("3", "Книга", "PRICE", "5", "2", "PLUS")
        );
    }

    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L})
    @ParameterizedTest
    public void test_getProduct(Long id) throws Exception {
        when(productService.findProductById(id)).thenReturn(buildProductByIdWithCartItem(1L, 0));

        mockMvc.perform(get("/items/{id}", id))
                .andExpect(model().attribute("item", mapToProductDto(buildProductByIdWithCartItem(1L, 0))))
                .andExpect(view().name("product_item"))
                .andExpect(status().isOk());
    }

    @MethodSource("arguments_test_changeProductCountInTheCart2")
    @ParameterizedTest
    public void test_changeProductCountInTheCart2(Long id, String actionType) throws Exception {
        ProductActionType actionParam = ProductActionType.valueOf(actionType);
        doNothing().when(cartItemService).changeProductCountInTheCart(id, actionParam);

        when(productService.findProductById(id)).thenReturn(buildProductByIdWithCartItem(id, 0));

        mockMvc.perform(post("/items/{id}", id)
                        .param("action", actionType))
                .andExpect(model().attribute("item", mapToProductDto(buildProductByIdWithCartItem(id, 0))))
                .andExpect(view().name("product_item"))
                .andExpect(status().isOk());
    }

    protected static Stream<Arguments> arguments_test_changeProductCountInTheCart2() {
        return Stream.of(
                Arguments.of(1L, "PLUS"),
                Arguments.of(2L, "MINUS"),
                Arguments.of(2L, "DELETE")
        );
    }
}