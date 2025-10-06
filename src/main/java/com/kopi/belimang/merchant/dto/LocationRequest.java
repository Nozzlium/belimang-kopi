package com.kopi.belimang.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationRequest {
    @NotNull(message = "Latitude is required")
    private Float lat;

    @NotNull(message = "Longitude is required")
    @JsonProperty("long")
    private Float lon;
}
