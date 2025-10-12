package com.kopi.belimang.order.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kopi.belimang.core.deserializer.StrictStringDeserializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlaceResponseBody {
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String orderId;
}
