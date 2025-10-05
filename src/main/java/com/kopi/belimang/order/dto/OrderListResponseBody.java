package com.kopi.belimang.order.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponseBody {
    private List<OrderGroup> orders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderGroup {
        private String orderId;
        private MerchantDTO merchant;
        private List<ItemDTO> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MerchantDTO {
        private String merchantId;
        private String name;
        private String merchantCategory;
        private String imageUrl;
        private LocationDTO location;
        private String createdAt; // ISO 8601 with nanoseconds
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationDTO {
        private double lat;
        private double lon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDTO {
        private String itemId;
        private String name;
        private String productCategory;
        private long price;
        private int quantity;
        private String imageUrl;
        private String createdAt; // ISO 8601 with nanoseconds
    }
}
