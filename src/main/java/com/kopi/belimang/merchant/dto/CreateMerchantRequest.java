package com.kopi.belimang.merchant.dto;


import com.kopi.belimang.merchant.mapper.MerchantCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMerchantRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
    private String name;

    @NotNull(message = "Merchant category is required")
    private String merchantCategory;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "^https?://.+\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$",
            message = "Image URL must be a valid URL ending with an image extension (jpg, jpeg, png, gif, webp)")
    private String imageUrl;

    @NotNull(message = "Location is required")
    private LocationRequest location;
}