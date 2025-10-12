package com.kopi.belimang.order.repostiory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kopi.belimang.core.entities.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    
    @Query(value = """
    SELECT
        od.id as orderDetailId,
        m.id AS merchantId,
        m.name AS merchantName,
        m.category AS merchantCategory,
        m.image_url AS merchantImageUrl,
        ST_Y(m.location::geometry) AS lat, 
        ST_X(m.location::geometry) AS lon,
        m.created_at AS merchantCreatedAt,
        i.id AS itemId,
        i.name AS itemName,
        i.category AS productCategory,
        i.price AS price,
        i.quantity AS quantity,
        i.image_url AS itemImageUrl,
        i.created_at AS itemCreatedAt,
        od.order_id as orderId
    FROM order_details od 
    LEFT JOIN merchants m ON od.merchant_id = m.id
    LEFT JOIN order_items i ON od.id = i.order_detail_id
    WHERE 
        od.order_id IN (:orderIds)
    """, nativeQuery = true)
    List<Object[]> findOrdersDetails(
        @Param("orderIds") List<Long> orderIds
    );
}
