package com.kopi.belimang.order.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyMerchantResponse {
    @Data
    @Builder
    public static class LocationResponse {
        private Double lat;
        @com.fasterxml.jackson.annotation.JsonProperty("long")
        private Double lon;
    }

    @Data
    @Builder
    public static class MerchantResponse {
        private String merchantId;
        private String name;
        private String merchantCategory;
        private String imageUrl;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        private ZonedDateTime createdAt;

        private LocationResponse location;
    }

    @Data
    @Builder
    public static class MerchantItemResponse {
        private String itemId;
        private String name;
        private String productCategory;
        private long price;
        private String imageUrl;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        private ZonedDateTime createdAt;
    }

    @Data
    @Builder
    public static class MetaResponse {
        private Integer limit;
        private Integer offset;
        private Long total;
    }

    @Data
    @Builder
    public static class MerchantAndItemResponse {
        MerchantResponse merchant;
        private List<MerchantItemResponse> items;
    }

    private MetaResponse meta;
    private List<MerchantAndItemResponse> data;
}
