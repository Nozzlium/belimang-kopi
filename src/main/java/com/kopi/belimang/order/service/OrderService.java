package com.kopi.belimang.order.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kopi.belimang.core.entities.Merchant;
import com.kopi.belimang.core.entities.MerchantItem;
import com.kopi.belimang.core.entities.Order;
import com.kopi.belimang.core.entities.OrderDetail;
import com.kopi.belimang.core.entities.OrderItem;
import com.kopi.belimang.merchant.dto.GetMerchantResponse;
import com.kopi.belimang.merchant.dto.MerchantSearchCriteria;
import com.kopi.belimang.merchant.repository.MerchantItemRepository;
import com.kopi.belimang.merchant.repository.MerchantRepository;
import com.kopi.belimang.order.dto.OrderRequestBody;
import com.kopi.belimang.order.dto.EstimateResponseBody;
import com.kopi.belimang.order.dto.GetAllOrdersParams;
import com.kopi.belimang.order.dto.OrderListResponseBody;
import com.kopi.belimang.order.dto.OrderPlaceRequestBody;
import com.kopi.belimang.order.dto.OrderPlaceResponseBody;
import com.kopi.belimang.order.dto.OrderRequestBody.OrderItemRequestBody;
import com.kopi.belimang.order.dto.OrderRequestBody.OrderMerchantRequestBody;
import com.kopi.belimang.order.dto.OrderRequestBody.UserLocation;
import com.kopi.belimang.order.repostiory.OrderDetailRepository;
import com.kopi.belimang.order.repostiory.OrderRepository;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantItemRepository merchantItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public EstimateResponseBody estimate(OrderRequestBody orderRequestBody) throws Exception, NumberFormatException {
        Long totalPrice = 0L;
        Map<Long, OrderDetail> resMap = new HashMap<>();

        List<OrderMerchantRequestBody> orders = orderRequestBody.getOrders();

        List<Merchant> merchants = new ArrayList<>();
        Map<Long, MerchantItem> itemIdToTemp = new HashMap<>(); 

        int startingIndex = -1;
        Set<Long> merchantIds = new HashSet<Long>();
        List<String> itemIds = new ArrayList<String>();
        
        for (int i = 0; i < orders.size(); i++) {
            OrderMerchantRequestBody order = orders.get(i);
            if (order.isStartingPoint()) {
                if (startingIndex != -1) throw new Exception();
                startingIndex = i;
            }
            merchantIds.add(Long.parseLong(order.getMerchantId()));

            for (OrderItemRequestBody item : order.getItems()) {
                Long itemId = Long.parseLong(item.getItemId());
                itemIds.add(item.getItemId());
                itemIdToTemp.put(itemId, MerchantItem
                    .builder()
                    .id(itemId)
                    .quantity(item.getQuantity())
                    .build()
                );
            }
        }

        List<MerchantItem> merchantItems = merchantItemRepository.findMerchangItemsWithMerchantByMerchantItemId(itemIds);
        if (merchantItems.size() != itemIds.size()) throw new EntityNotFoundException("One or more item IDs do not exist.");

        for (MerchantItem merchantItem : merchantItems) {
            Merchant merchant = merchantItem.getMerchant();
            OrderDetail orderDetail;
            if (resMap.containsKey(merchant.getId())) {
                orderDetail = resMap.get(merchant.getId());
            } else {
                orderDetail = OrderDetail
                    .builder()
                    .merchant(merchant)
                    .items(new ArrayList<>())
                    .build();
                resMap.put(merchant.getId(), orderDetail);
            }
            if (merchant == null || !merchants.contains(merchant)) throw new EntityNotFoundException("One or more merchant IDs do not exist.");
            
            MerchantItem temp = itemIdToTemp.get(merchantItem.getId());
            if (merchant.getId() != temp.getMerchant().getId()) throw new Exception("One or more items do not belong to the specified merchant.");

            totalPrice += merchantItem.getPrice() * temp.getQuantity();
            merchants.add(merchant);
            orderDetail.getItems().add(OrderItem
                .builder()
                .itemId(merchantItem.getId())
                .orderDetail(orderDetail)
                .category(merchantItem.getCategory())
                .name(merchantItem.getName())
                .price(merchantItem.getPrice())
                .imageUrl(merchantItem.getImageUrl())
                .quantity(temp.getQuantity())
                .createdAt(merchantItem.getCreatedAt())
                .build()
            );
        }

        if (merchants.size() != merchantIds.size()) {
            throw new EntityNotFoundException("One or more merchant IDs do not exist.");
        }
        
        long[][] distanceMatrix = generateDistanceMatrix(orderRequestBody.getUserLocation(), merchants);
        long distanceEstimate = solveTsp(distanceMatrix, startingIndex);
        long estimateDeliveryTime = distanceEstimate / 40000; // in minutes

        List<OrderDetail> res = resMap.values().stream().toList();
        Order toSave = Order
                .builder()
                .totalDistance(distanceEstimate)
                .estimatedDeliveryTime(estimateDeliveryTime)
                .totalPrice(totalPrice)
                .orderDetails(res)
                .build();
        orderRepository.save(toSave);
        
        return EstimateResponseBody
            .builder()
            .calculatedEstimateId(toSave.getId())
            .estimatedDeliveryTimeInMinutes(toSave.getEstimatedDeliveryTime())
            .totalPrice(toSave.getTotalPrice())
            .build();
    }

    public OrderPlaceResponseBody placeOrder(OrderPlaceRequestBody requestBody) throws EntityNotFoundException {
        Long orderId;
        try {
            orderId = Long.parseLong(requestBody.getCalculatedEstimateId());
        } catch (NumberFormatException e) {
            throw new EntityNotFoundException("Calculated estimate ID is not valid.");
        }

        Order order = orderRepository.updateHasOrderMadeFlag(orderId);
        if (order == null) throw new EntityNotFoundException("Calculated estimate ID does not exist or order has already been placed.");

        return OrderPlaceResponseBody
            .builder()
            .orderId(order.getId().toString())
            .build();
    }

    
    @Transactional
    public GetMerchantResponse searchNearbyMerchants(String latStr, String lonStr, MerchantSearchCriteria criteria) {
        int validatedLimit = criteria.getValidatedLimit();
        int validatedOffset = criteria.getValidatedOffset();

        double lat, lon;
        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
                return GetMerchantResponse.builder()
                        .data(new ArrayList<>())
                        .meta(GetMerchantResponse.MetaResponse.builder()
                                .limit(validatedLimit)
                                .offset(validatedOffset)
                                .total(0L)
                                .build())
                        .build();
        }

        List<Merchant> merchants = merchantRepository.findNearbyMerchants(lat, lon, criteria.getMerchantId(), criteria.getName(), criteria.getMerchantCategory(), validatedLimit, validatedOffset);

        long total = merchantRepository.countWithFilters(
                criteria.getMerchantId(),
                criteria.getName(),
                criteria.getMerchantCategory()
        );

        List<GetMerchantResponse.MerchantResponse> merchantData = merchants.stream()
                .map(this::convertToMerchantResponse)
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

    public OrderListResponseBody findAllOrders(GetAllOrdersParams params) {
        int limit;
        int offset;
        Long merchantId = null;
        String name = null;
        String merchantCategory = null;

        try {
            limit = params.getLimit();
            offset = params.getOffset();
            if (params.getMerchantId() != null) {
                merchantId = Long.parseLong(params.getMerchantId());
            }
            name = params.getName();
            merchantCategory = params.getMerchantCategory();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("One or more query parameters are not valid.");
        }

        List<Order> orders = orderDetailRepository.findOrders(limit, offset, merchantId, name, merchantCategory);
        List<OrderListResponseBody.OrderGroup> orderGroups = new ArrayList<>();

        for (Order order : orders) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                OrderListResponseBody.MerchantDTO merchantDTO = OrderListResponseBody.MerchantDTO
                    .builder()
                    .merchantId(orderDetail.getMerchant().getId().toString())
                    .name(orderDetail.getMerchant().getName())
                    .merchantCategory(orderDetail.getMerchant().getCategory())
                    .imageUrl(orderDetail.getMerchant().getImageUrl())
                    .location(OrderListResponseBody.LocationDTO
                        .builder()
                        .lat(orderDetail.getMerchant().getLocation().getY())
                        .lon(orderDetail.getMerchant().getLocation().getX())
                        .build()
                    )
                    .createdAt(orderDetail.getMerchant().getCreatedAt().toString())
                    .build();

                List<OrderListResponseBody.ItemDTO> itemDTOs = new ArrayList<>();
                for (OrderItem item : orderDetail.getItems()) {
                    itemDTOs.add(OrderListResponseBody.ItemDTO
                        .builder()
                        .itemId(item.getItemId().toString())
                        .name(item.getName())
                        .productCategory(item.getCategory())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .imageUrl(item.getImageUrl())
                        .createdAt(item.getCreatedAt().toString())
                        .build()
                    );
                }

                orderGroups.add(OrderListResponseBody.OrderGroup
                    .builder()
                    .orderId(order.getId().toString())
                    .merchant(merchantDTO)
                    .items(itemDTOs)
                    .build()
                );
            }
        }

        return OrderListResponseBody
            .builder()
            .orders(orderGroups)
            .build();
    }

    public long solveTsp(long[][] distanceMatrix, int startingIndex) throws Exception {
        Loader.loadNativeLibraries();

        int size = distanceMatrix.length;
        RoutingIndexManager manager = new RoutingIndexManager(size, 1, startingIndex); // 0 is the starting index
        RoutingModel routing = new RoutingModel(manager);

        int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });

        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
            .toBuilder()
            .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
            .build();

        Assignment solution = routing.solveWithParameters(searchParameters);

        if (solution != null) {
            long totalDistance = 0;
            long index = routing.start(0);
            while (!routing.isEnd(index)) {
                long previousIndex = index;
                index = solution.value(routing.nextVar(index));
                totalDistance += routing.getArcCostForVehicle(previousIndex, index, 0);
            }
            return totalDistance;
        } else {
            throw new Exception();
        }
    }

    private long[][] generateDistanceMatrix(UserLocation userLocation, List<Merchant> merchants) throws Exception {
        long[][] distanceMatrix = new long[merchants.size()][merchants.size()];
        int startingJ = 0;
        for (int i = 0; i < merchants.size(); i++) {
            for (int j = startingJ; i < merchants.size(); j++) {
                Merchant starting = merchants.get(i);
                long distanceFromUser = haversine(
                    userLocation.getLat(),
                    userLocation.getLat(),
                    starting.getLocation().getY(),
                    starting.getLocation().getX()
                );
                if (distanceFromUser > 3000) throw new Exception();

                if (j != i) {
                    Merchant dest = merchants.get(j);
                    long distance = haversine(
                        starting.getLocation().getY(),
                        starting.getLocation().getX(),
                        dest.getLocation().getY(),
                        dest.getLocation().getX()
                    );
                    distanceMatrix[i][j] = distance;
                    distanceMatrix[j][i] = distance;
                }
            }
            startingJ++;
        }
        return distanceMatrix;
    }

    private long haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (long)(R * c); // distance in meters
    }

    private GetMerchantResponse.MerchantResponse convertToMerchantResponse(Merchant merchant) {
        return GetMerchantResponse.MerchantResponse.builder()
                .merchantId(merchant.getId())
                .name(merchant.getName())
                .merchantCategory(merchant.getCategory())
                .imageUrl(merchant.getImageUrl())
                .location(extractLocation(merchant.getLocation()))
                .createdAt(merchant.getCreatedAt())
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
