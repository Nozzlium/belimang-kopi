package com.kopi.belimang.merchant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kopi.belimang.merchant.mapper.MerchantCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class GetMerchantResponse {

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
        private Long merchantId;
        private String name;
        private String merchantCategory;
        private String imageUrl;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'")
        private ZonedDateTime createdAt;

        private LocationResponse location;
    }

    @Data
    @Builder
    public static class MetaResponse {
        private Integer limit;
        private Integer offset;
        private Long total;
    }

    private MetaResponse meta;
    private List<MerchantResponse> data;
}