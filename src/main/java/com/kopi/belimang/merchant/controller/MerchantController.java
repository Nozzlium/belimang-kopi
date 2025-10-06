package com.kopi.belimang.merchant.controller;

import com.kopi.belimang.auth.core.guard.Guard;
import com.kopi.belimang.merchant.dto.*;
import com.kopi.belimang.merchant.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
@Slf4j
public class MerchantController {
    private final MerchantService merchantService;

    @PostMapping
    @Guard(acceptedRoles = {"ADMIN"})
    public ResponseEntity<CreateMerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request){

        if (request.getMerchantCategory() != null && !VALID_CATEGORIES.contains(request.getMerchantCategory())) {
            throw new IllegalArgumentException("Invalid merchant category: " + request.getMerchantCategory());
        }
        if (request.getLocation() == null) {
            throw new IllegalArgumentException("Location is required");
        }

        LocationRequest location = request.getLocation();

        if (location.getLat() == null) {
            throw new IllegalArgumentException("Latitude is required");
        }

        if (location.getLon() == null) {
            throw new IllegalArgumentException("Longitude is required");
        }

        CreateMerchantResponse response = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Guard(acceptedRoles = {"ADMIN"})
    public ResponseEntity<GetMerchantResponse> getMerchants(
            @RequestParam(required = false) String merchantId,
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String merchantCategory,
            @RequestParam(defaultValue = "desc") String createdAt
    ) {
        if (merchantCategory != null && !VALID_CATEGORIES.contains(merchantCategory)) {
            throw new IllegalArgumentException("Invalid merchant category: " + merchantCategory);
        }
        MerchantSearchCriteria criteria = MerchantSearchCriteria.builder()
                .merchantId(parseStringToLong(merchantId))
                .name(name)
                .merchantCategory(merchantCategory)
                .createdAt(createdAt)
                .limit(limit)
                .offset(offset)
                .build();

        GetMerchantResponse response = merchantService.searchMerchants(criteria);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{merchantId}/items")
    @Guard(acceptedRoles = {"ADMIN"})
    public ResponseEntity<CreateMerchantItemResponse> createMerchantItem(
            @PathVariable Long merchantId,
            @Valid @RequestBody CreateMerchantItemRequest request
    ){

        if (request.getProductCategory() != null && !VALID_PRODUCT_CATEGORIES.contains(request.getProductCategory())) {
            throw new IllegalArgumentException("Invalid product category: " + request.getProductCategory());
        }

        CreateMerchantItemResponse response= merchantService.createMerchantItem(merchantId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{merchantId}/items")
    @Guard(acceptedRoles = {"ADMIN"})
    public ResponseEntity<GetMerchantItemResponse> getMerchantItems(
            @PathVariable Long merchantId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String productCategory,
            @RequestParam(defaultValue = "desc") String createdAt
    ) {
        if (productCategory != null && !VALID_PRODUCT_CATEGORIES.contains(productCategory)) {
            throw new IllegalArgumentException("Invalid product category: " + productCategory);
        }
        MerchantItemSearchCriteria merchantItemSearchCriteria = MerchantItemSearchCriteria.builder()
                .itemId(itemId)
                .name(name)
                .productCategory(productCategory)
                .createdAt(createdAt)
                .limit(limit)
                .offset(offset)
                .build();

        GetMerchantItemResponse response = merchantService.searchMerchantItems(merchantId,merchantItemSearchCriteria);

        return ResponseEntity.ok(response);
    }
    private Long parseStringToLong(String id) {
        if (id != null && !id.trim().isEmpty()) {
            try {
                return Long.parseLong(id);
            } catch (NumberFormatException e) {
                log.warn("Invalid id format: {}", id);
                return null;
            }
        }
        return null;
    }
    private static final Set<String> VALID_CATEGORIES = Set.of(
            "SmallRestaurant", "MediumRestaurant", "LargeRestaurant",
            "MerchandiseRestaurant", "BoothKiosk", "ConvenienceStore"
    );

    private static final Set<String> VALID_PRODUCT_CATEGORIES = Set.of(
            "Beverage","Food","Snack","Condiments","Additions"
    );
}
