package com.kopi.belimang.order.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponseBody {
    private String orderId;
    private List<OrderGroup> orders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderGroup {
        @JsonIgnore
        private Long orderId;
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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        private ZonedDateTime createdAt; // ISO 8601 with nanoseconds
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationDTO {
        private double lat;
        @JsonProperty("long")
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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        private ZonedDateTime createdAt; // ISO 8601 with nanoseconds
    }
}
