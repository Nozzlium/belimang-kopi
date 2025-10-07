package com.kopi.belimang.order.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kopi.belimang.core.entities.Merchant;
import com.kopi.belimang.core.entities.MerchantItem;
import com.kopi.belimang.core.entities.Order;
import com.kopi.belimang.core.entities.OrderDetail;
import com.kopi.belimang.core.entities.OrderItem;
import com.kopi.belimang.merchant.dto.MerchantSearchCriteria;
import com.kopi.belimang.merchant.exception.MerchantNotFoundException;
import com.kopi.belimang.merchant.repository.MerchantItemRepository;
import com.kopi.belimang.merchant.repository.MerchantRepository;
import com.kopi.belimang.order.dto.OrderRequestBody;
import com.kopi.belimang.order.dto.NearbyMerchantResponse.LocationResponse;
import com.kopi.belimang.order.dto.NearbyMerchantResponse.MerchantAndItemResponse;
import com.kopi.belimang.order.dto.NearbyMerchantResponse.MerchantItemResponse;
import com.kopi.belimang.order.dto.NearbyMerchantResponse.MerchantResponse;
import com.kopi.belimang.order.dto.EstimateResponseBody;
import com.kopi.belimang.order.dto.GetAllOrdersParams;
import com.kopi.belimang.order.dto.NearbyMerchantResponse;
import com.kopi.belimang.order.dto.OrderListResponseBody;
import com.kopi.belimang.order.dto.OrderPlaceRequestBody;
import com.kopi.belimang.order.dto.OrderPlaceResponseBody;
import com.kopi.belimang.order.dto.OrderRequestBody.OrderItemRequestBody;
import com.kopi.belimang.order.dto.OrderRequestBody.OrderMerchantRequestBody;
import com.kopi.belimang.order.dto.OrderRequestBody.UserLocation;
import com.kopi.belimang.order.exception.ItemAndMerchantMismatchException;
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

    public EstimateResponseBody estimate(OrderRequestBody orderRequestBody) throws ItemAndMerchantMismatchException, MerchantNotFoundException, NumberFormatException, Exception {
        Long totalPrice = 0L;

        List<OrderMerchantRequestBody> orders = orderRequestBody.getOrders();
        if (orders == null) throw new IllegalArgumentException("Orders cannot be empty.");

        List<Merchant> tempMerchants = new ArrayList<>();

        int startingIndex = -1;
        Set<Long> merchantIds = new HashSet<Long>();
        List<Long> itemIds = new ArrayList<Long>();
        
        System.out.println(orderRequestBody);
        for (int i = 0; i < orders.size(); i++) {
            OrderMerchantRequestBody order = orders.get(i);
            if (order.isStartingPoint()) {
                if (startingIndex != -1) throw new IllegalArgumentException("Starting point must be specified, status: " + startingIndex);
                startingIndex = i;
            }
            merchantIds.add(Long.parseLong(order.getMerchantId()));
            Merchant tempMerch = Merchant
                .builder()
                .id(Long.parseLong(order.getMerchantId()))
                .items(new ArrayList<>())
                .build();

            if (order.getItems() == null || order.getItems().size() == 0) {
                throw new IllegalArgumentException("Items cannot be empty.");
            }
            for (OrderItemRequestBody item : order.getItems()) {
                Long itemId = Long.parseLong(item.getItemId());
                itemIds.add(itemId);
                MerchantItem tempItem = MerchantItem
                    .builder()
                    .id(itemId)
                    .quantity(item.getQuantity())
                    .merchant(tempMerch)
                    .build();
                tempMerch.getItems().add(tempItem);
            }
            tempMerchants.add(tempMerch);
        }
        if (startingIndex == -1) throw new IllegalArgumentException("Starting point must be specified, status: " + startingIndex);

        List<Object[]> merchantItems = merchantItemRepository.findMerchangItemsWithMerchantByMerchantItemId(itemIds);
        if (merchantItems.size() != itemIds.size()) throw new ItemAndMerchantMismatchException("One or more item IDs do not exist.");

        Map<Long, MerchantItem> itemIdToTemp = new HashMap<>();
        for (Object[] row : merchantItems) {
            Double lat = row[6] != null ? (Double) row[6] : null;
            Double lon = row[7] != null ? (Double) row[7] : null;
            if (lat == null || lon == null) throw new ItemAndMerchantMismatchException("One or more merchant IDs do not exist.");
            GeometryFactory geometryFactory = new GeometryFactory();
            Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
            MerchantItem mi = MerchantItem
                .builder()
                .id(((Number) row[0]).longValue())
                .merchant(Merchant
                    .builder()
                    .id(((Number) row[1]).longValue())
                    .location(point)
                    .build())
                .category((String) row[2])
                .name((String) row[3])
                .price(row[4] != null ? ((Number) row[4]).longValue() : 0)
                .imageUrl((String) row[5])
                .build();
            itemIdToTemp.put(mi.getId(), mi);
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Merchant tempMerch : tempMerchants) {
            OrderDetail od = OrderDetail
                .builder()
                .merchant(Merchant
                    .builder()
                    .id(tempMerch.getId())
                    .build())
                .items(new ArrayList<>())
                .build();
            for (MerchantItem tempItem : tempMerch.getItems()) {
                MerchantItem realItem = itemIdToTemp.get(tempItem.getId());
                if (realItem == null || !realItem.getMerchant().getId().equals(tempMerch.getId())) {
                    throw new ItemAndMerchantMismatchException("One or more item IDs do not exist.");
                }

                tempMerch.setLocation(realItem.getMerchant().getLocation());
                OrderItem oi = OrderItem
                    .builder()
                    .itemId(realItem.getId())
                    .name(realItem.getName())
                    .category(realItem.getCategory())
                    .price(realItem.getPrice())
                    .quantity(tempItem.getQuantity())
                    .imageUrl(realItem.getImageUrl())
                    .createdAt(ZonedDateTime.now(ZoneId.systemDefault()))
                    .build();
                od.getItems().add(oi);
                totalPrice += realItem.getPrice() * tempItem.getQuantity();
            }
            orderDetails.add(od);
        }

        if (
            (tempMerchants.size() != orderDetails.size())
        ) {
            throw new ItemAndMerchantMismatchException("One or more merchant IDs do not exist.");
        }
        
        long[][] distanceMatrix = generateDistanceMatrix(orderRequestBody.getUserLocation(), tempMerchants);
        long distanceEstimate = solveTsp(distanceMatrix, startingIndex);
        long estimateDeliveryTime = distanceEstimate / 40000; // in minutes

        Order toSave = Order
                .builder()
                .totalDistance(distanceEstimate)
                .estimatedDeliveryTime(estimateDeliveryTime)
                .totalPrice(totalPrice)
                .orderDetails(orderDetails)
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
    public NearbyMerchantResponse searchNearbyMerchants(String latStr, String lonStr, MerchantSearchCriteria criteria) {
        int validatedLimit = criteria.getValidatedLimit();
        int validatedOffset = criteria.getValidatedOffset();

        double lat, lon;
        try {
            lat = Double.parseDouble(latStr);
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
                throw e;
        }

        List<Object[]> merchants = merchantRepository.findNearbyMerchants(lat, lon, criteria.getMerchantId(), criteria.getName(), criteria.getMerchantCategory(), validatedLimit, validatedOffset);
        Map<Long, MerchantAndItemResponse> merchantMap = new HashMap<>();
        for (Object[] row : merchants) {
            Long merchantId = ((Number) row[0]).longValue();
            MerchantAndItemResponse merchAndItem = merchantMap.containsKey(merchantId) ? merchantMap.get(merchantId) : null;
            if (merchAndItem == null) {    
                String merchantName = (String) row[1];
                String merchantCategory = (String) row[2];
                String imageUrl = (String) row[3];
                Double latt = (Double) row[4]; // handle as needed
                Double lonn = (Double) row[5]; // handle as needed
                Object createdAt = row[6]; // handle as needed
                ZonedDateTime zonedCreatedAt = null;
                if (createdAt instanceof java.sql.Timestamp) {
                    zonedCreatedAt = ((java.sql.Timestamp) createdAt).toInstant().atZone(ZoneId.systemDefault());
                }
                
                merchAndItem = MerchantAndItemResponse
                    .builder()
                    .merchant(MerchantResponse.builder()
                        .merchantId(merchantId.toString())
                        .name(merchantName)
                        .merchantCategory(merchantCategory)
                        .imageUrl(imageUrl)
                        .location(LocationResponse.builder()
                            .lat(latt)
                            .lon(lonn)
                            .build())
                        .createdAt(zonedCreatedAt)
                        .build())
                    .items(new ArrayList<>())
                    .build();
                merchantMap.put(merchantId, merchAndItem);
            }
            Long itemId = row[7] != null ? ((Number) row[7]).longValue() : null;
            if (itemId == null) continue; // no item associated
            String itemName = (String) row[8];
            String productCategory = (String) row[9];
            Long price = row[10] != null ? ((Number) row[10]).longValue() : 0;
            String itemImageUrl = (String) row[11];
            Object itemCreatedAt = row[12]; // handle as needed
            ZonedDateTime itemZonedCreatedAt = null;
            if (itemCreatedAt instanceof java.sql.Timestamp) {
                itemZonedCreatedAt = ((java.sql.Timestamp) itemCreatedAt).toInstant().atZone(ZoneId.systemDefault());
            }
            MerchantItemResponse item = MerchantItemResponse.builder()
                .itemId(itemId.toString())
                .name(itemName)
                .productCategory(productCategory)
                .price(price)
                .imageUrl(itemImageUrl)
                .createdAt(itemZonedCreatedAt)
                .build();
            merchAndItem.getItems().add(item);
        }

        long total = merchantRepository.countWithFilters(
                criteria.getMerchantId(),
                criteria.getName(),
                criteria.getMerchantCategory()
        );

        return NearbyMerchantResponse.builder()
                .data(merchantMap.values().stream().toList())
                .meta(NearbyMerchantResponse.MetaResponse.builder()
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

    private long[][] generateDistanceMatrix(UserLocation userLocation, List<Merchant> merchants) throws MerchantNotFoundException {
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
                if (distanceFromUser > 3000) throw new MerchantNotFoundException("Merchant with id " + starting.getId() + " is too far: " + distanceFromUser + " meters (max: 3000 meters)");

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

}
