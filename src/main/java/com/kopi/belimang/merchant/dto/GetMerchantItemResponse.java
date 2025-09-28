package com.kopi.belimang.merchant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class GetMerchantItemResponse {
    @Data
    @Builder
    public static class MerchantItemResponse {
        private Long itemId;
        private String name;
        private String productCategory;
        private Long price;
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

    private GetMerchantItemResponse.MetaResponse meta;
    private List<GetMerchantItemResponse.MerchantItemResponse> data;
}
