package com.payment.payment.controller;

import com.payment.payment.dto.CreatePaymentOrderRequest;
import com.payment.payment.dto.PaymentOrderResponse;
import com.payment.payment.dto.RazorpayOrderResponse;
import com.payment.payment.dto.UpdateRazorpayOrderRequest;
import com.payment.payment.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Payment order operations")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a payment order")
    public PaymentOrderResponse createOrder(@Valid @RequestBody CreatePaymentOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    @Operation(summary = "List payment orders")
    public List<RazorpayOrderResponse> listOrders(@RequestParam(defaultValue = "10") int count, @RequestParam(defaultValue = "0") int skip) {
        return orderService.listOrders(count, skip);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get a payment order")
    public RazorpayOrderResponse getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId);
    }

    @PatchMapping("/{orderId}")
    @Operation(summary = "Update a payment order")
    public RazorpayOrderResponse updateOrder(@PathVariable String orderId, @Valid @RequestBody UpdateRazorpayOrderRequest request) {
        return orderService.updateOrder(orderId, request);
    }
}