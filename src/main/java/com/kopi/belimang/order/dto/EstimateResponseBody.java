package com.kopi.belimang.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimateResponseBody {
    private String calculatedEstimateId;
    private Long estimatedDeliveryTimeInMinutes;
    private Long totalPrice;
}
