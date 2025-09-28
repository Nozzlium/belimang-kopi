package com.kopi.belimang.merchant.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class MerchantItemSearchCriteria {
    private Long itemId;
    private String name;
    private  String createdAt;
    private String productCategory;
    private Integer limit;
    private Integer offset;

    public String getValidatedSortOrder(){
        if("asc".equalsIgnoreCase(createdAt)||"desc".equalsIgnoreCase(createdAt)){
            return createdAt.toLowerCase();
        }
        return null;
    }

    public Integer getValidatedLimit() {
        return (limit != null && limit > 0) ? Math.min(limit, 100) : 10; // max 100
    }

    public Integer getValidatedOffset() {
        return (offset != null && offset >= 0) ? offset : 0;
    }
}
