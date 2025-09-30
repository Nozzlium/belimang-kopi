package com.kopi.belimang.merchant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMerchantItemRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
    private String name;

    @NotNull(message = "Product category is required")
    private String productCategory;



    @NotNull(message = "Location is required")
    @Min(1)
    private Long price;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "^https?://.+\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$",
            message = "Image URL must be a valid URL ending with an image extension (jpg, jpeg, png, gif, webp)")
    private String imageUrl;
}
