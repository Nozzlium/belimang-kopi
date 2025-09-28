package com.kopi.belimang.merchant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateMerchantResponse {
    private String merchantId;
}
