package com.kopi.belimang.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kopi.belimang.merchant.dto.GetMerchantResponse;
import com.kopi.belimang.order.dto.EstimateResponseBody;
import com.kopi.belimang.order.dto.GetAllOrdersParams;
import com.kopi.belimang.order.dto.OrderListResponseBody;
import com.kopi.belimang.order.dto.OrderPlaceRequestBody;
import com.kopi.belimang.order.dto.OrderPlaceResponseBody;
import com.kopi.belimang.order.dto.OrderRequestBody;
import com.kopi.belimang.order.service.OrderService;

@RestController
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    // GET /merchants/nearby/{lat},{long}
    @GetMapping("/merchants/nearby/{lat},{long}")
    public ResponseEntity<GetMerchantResponse> getNearbyMerchants(@PathVariable String lat, @PathVariable("long") String lon) {
        GetMerchantResponse resp = orderService.searchNearbyMerchants(lat, lon, null);

        return ResponseEntity.ok().body(resp);
    }

    // POST /users/estimate
    @PostMapping("/users/estimate")
    public ResponseEntity<EstimateResponseBody> estimateOrder(
        @RequestBody OrderRequestBody requestBody
    ) throws Exception {
        EstimateResponseBody resp = orderService.estimate(requestBody);

        return ResponseEntity.ok().body(resp);
    }

    // POST /users/orders
    @PostMapping("/users/orders")
    public ResponseEntity<OrderPlaceResponseBody> createOrder(
        @RequestBody OrderPlaceRequestBody requestBody
    ) {
        OrderPlaceResponseBody resp = orderService.placeOrder(requestBody);

        return ResponseEntity.ok().body(resp);
    }

    // GET /users/orders
    @GetMapping("/users/orders")
    public ResponseEntity<OrderListResponseBody> getOrders(
        @RequestParam(required = false) String merchantId,
        @RequestParam(required = false) String limit,
        @RequestParam(required = false) String offset,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String merchantCategory
    ) {
        int limitVal = 5;
        int offsetVal = 0;
        try {
            if (limit != null) limitVal = Integer.parseInt(limit);
        } catch (NumberFormatException e) { /* use default */ }
        try {
            if (offset != null) offsetVal = Integer.parseInt(offset);
        } catch (NumberFormatException e) { /* use default */ }

        GetAllOrdersParams params = GetAllOrdersParams.builder()
            .merchantId(merchantId)
            .limit(limitVal)
            .offset(offsetVal)
            .name(name)
            .merchantCategory(merchantCategory)
            .build();
        OrderListResponseBody resp = orderService.findAllOrders(params);

        return ResponseEntity.ok().body(resp);
    }
    
}
