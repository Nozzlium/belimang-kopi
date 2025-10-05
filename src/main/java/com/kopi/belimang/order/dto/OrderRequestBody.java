package com.kopi.belimang.order.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderRequestBody {
    private UserLocation userLocation;
    private List<OrderMerchantRequestBody> orders;

    @Data
    @NoArgsConstructor
    public static class UserLocation {
        private float lat;
        private float lon;
    }

    @Data
    @NoArgsConstructor
    public static class OrderMerchantRequestBody {
        private String merchantId;
        private boolean isStartingPoint;
        private List<OrderItemRequestBody> items;
    }

    @Data
    @NoArgsConstructor
    public static class OrderItemRequestBody {
        private String itemId;
        private int quantity;
    }
}
