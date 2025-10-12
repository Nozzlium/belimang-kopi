package com.kopi.belimang.order.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kopi.belimang.core.deserializer.StrictStringDeserializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestBody {
    private UserLocation userLocation;
    private List<OrderMerchantRequestBody> orders;

    @Data
    @NoArgsConstructor
    public static class UserLocation {
        private Double lat;
        @JsonProperty("long")
        private Double lon;
    }

    @Data
    @NoArgsConstructor
    public static class OrderMerchantRequestBody {
        @JsonDeserialize(using = StrictStringDeserializer.class)
        private String merchantId;
        @JsonProperty("isStartingPoint")
        private boolean startingPoint;
        private List<OrderItemRequestBody> items;
    }

    @Data
    @NoArgsConstructor
    public static class OrderItemRequestBody {
        @JsonDeserialize(using = StrictStringDeserializer.class)
        private String itemId;
        private int quantity;
    }
}
