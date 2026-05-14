package com.travelnest.order.controller;

import com.travelnest.common.api.ApiResponse;
import com.travelnest.order.dto.CreateOrderRequest;
import com.travelnest.order.dto.OrderResponse;
import com.travelnest.order.service.OrderService;
import com.travelnest.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createDraftOrder(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Checkout draft saved successfully",
                orderService.createDraftOrder(authenticatedUser, request)
        ));
    }
}
