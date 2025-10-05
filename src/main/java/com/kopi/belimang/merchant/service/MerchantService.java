package com.kopi.belimang.merchant.service;

import com.kopi.belimang.core.entities.Merchant;
import com.kopi.belimang.core.entities.MerchantItem;
import com.kopi.belimang.merchant.dto.*;
import com.kopi.belimang.merchant.exception.MerchantNotFoundException;
import com.kopi.belimang.merchant.repository.MerchantItemRepository;
import com.kopi.belimang.merchant.repository.MerchantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantItemRepository merchantItemRepository;

    @Transactional
    public CreateMerchantResponse createMerchant(CreateMerchantRequest request) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = geometryFactory.createPoint(new Coordinate(request.getLocation().getLon(), request.getLocation().getLat()));
        Merchant merchant = new Merchant();
        merchant.setCategory(request.getMerchantCategory());
        merchant.setName(request.getName());
        merchant.setImageUrl(request.getImageUrl());
        merchant.setLocation(point);
        Long resultId= merchantRepository.save(merchant).getId();
        return CreateMerchantResponse.builder().merchantId(resultId.toString()).build();
    }

    @Transactional
    public GetMerchantResponse searchMerchants(MerchantSearchCriteria criteria) {
        log.debug("Searching merchants with criteria: {}", criteria);

        String validatedSortOrder = criteria.getValidatedSortOrder();
        int validatedLimit = criteria.getValidatedLimit();
        int validatedOffset = criteria.getValidatedOffset();

        List<Merchant> merchantPage = merchantRepository.findMerchantsWithFilters(
                criteria.getMerchantId(),
                criteria.getName(),
                criteria.getMerchantCategory() != null ? criteria.getMerchantCategory() : null,
                validatedSortOrder,
                validatedLimit,
                validatedOffset
        );

        long total = merchantRepository.countWithFilters(
                criteria.getMerchantId(),
                criteria.getName(),
                criteria.getMerchantCategory()
        );

        List<GetMerchantResponse.MerchantResponse> merchantData = merchantPage.stream()
                .map(this::convertToMerchantItemResponse)
                .collect(Collectors.toList());

        return GetMerchantResponse.builder()
                .data(merchantData)
                .meta(GetMerchantResponse.MetaResponse.builder()
                        .limit(validatedLimit)
                        .offset(validatedOffset)
                        .total(total)
                        .build())
                .build();
    }


    @Transactional
    public CreateMerchantItemResponse createMerchantItem(Long merchantId,CreateMerchantItemRequest request) {
        merchantRepository.findById(merchantId).orElseThrow(()-> new MerchantNotFoundException(merchantId));
        MerchantItem merchantItem = MerchantItem.builder()
                .name(request.getName())
                .merchant(Merchant.builder().id(merchantId).build())
                .price(request.getPrice())
                .category(request.getProductCategory())
                .imageUrl(request.getImageUrl())
                .build();
        Long resultId= merchantItemRepository.save(merchantItem).getId();
        return CreateMerchantItemResponse.builder().itemId(resultId.toString()).build();
    }

    @Transactional
    public GetMerchantItemResponse searchMerchantItems(Long merchantId,MerchantItemSearchCriteria merchantItemSearchCriteria) {
        log.debug("Searching merchants Item with criteria: {}", merchantItemSearchCriteria);

        String validatedSortOrder = merchantItemSearchCriteria.getValidatedSortOrder();
        int validatedLimit = merchantItemSearchCriteria.getValidatedLimit();
        int validatedOffset = merchantItemSearchCriteria.getValidatedOffset();

        List<MerchantItem> data= merchantItemRepository.findMerchantItemsWithFilters(
                merchantId,
                merchantItemSearchCriteria.getName(),
                merchantItemSearchCriteria.getProductCategory(),
                validatedSortOrder,
                validatedLimit,
                validatedOffset
        );

        Long total = merchantItemRepository.countWithFilters(merchantId, merchantItemSearchCriteria.getName(), merchantItemSearchCriteria.getProductCategory());

        List<GetMerchantItemResponse.MerchantItemResponse> merchantItemData= data.stream()
                .map(this::convertToMerchantItemResponse)
                .toList();

        return GetMerchantItemResponse.builder()
                .data(merchantItemData)
                .meta(GetMerchantItemResponse.MetaResponse.builder()
                        .limit(validatedLimit)
                        .offset(validatedOffset)
                        .total(total)
                        .build())
                .build();
    }

    private GetMerchantResponse.MerchantResponse convertToMerchantItemResponse(Merchant merchant) {
        return GetMerchantResponse.MerchantResponse.builder()
                .merchantId(merchant.getId())
                .name(merchant.getName())
                .merchantCategory(merchant.getCategory())
                .imageUrl(merchant.getImageUrl())
                .location(extractLocation(merchant.getLocation()))
                .createdAt(merchant.getCreatedAt())
                .build();
    }

    private GetMerchantItemResponse.MerchantItemResponse convertToMerchantItemResponse(MerchantItem merchantItem) {
        return GetMerchantItemResponse.MerchantItemResponse.builder()
                .itemId(merchantItem.getId())
                .name(merchantItem.getName())
                .price(merchantItem.getPrice())
                .productCategory(merchantItem.getCategory())
                .imageUrl(merchantItem.getImageUrl())
                .createdAt(merchantItem.getCreatedAt())
                .build();
    }

    private GetMerchantResponse.LocationResponse extractLocation(Point point) {
        if (point == null) {
            return GetMerchantResponse.LocationResponse.builder()
                    .lat(null)
                    .lon(null)
                    .build();
        }

        return GetMerchantResponse.LocationResponse.builder()
                .lat(point.getY())
                .lon(point.getX())
                .build();
    }
}
