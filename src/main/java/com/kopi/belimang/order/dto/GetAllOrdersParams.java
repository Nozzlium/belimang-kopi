package com.kopi.belimang.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllOrdersParams {
    private int limit;
    private int offset;
    private String merchantId;
    private String name;
    private String merchantCategory;
}
