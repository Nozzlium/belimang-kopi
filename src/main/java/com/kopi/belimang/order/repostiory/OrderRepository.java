package com.kopi.belimang.order.repostiory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kopi.belimang.core.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = """
        UPDATE orders
        SET has_order_made = true
        WHERE id = :orderId AND has_order_made = false
        RETURNING *
        """, nativeQuery = true)
    public Order updateHasOrderMadeFlag(@Param("orderId") Long orderId);

    @Query(value = """
    SELECT DISTINCT ON (o.id)
        o.id AS orderId,
        m.id AS merchantId,
        m.name AS merchantName,
        m.category AS merchantCategory,
        i.id AS itemId,
        i.name AS itemName,
        od.id as orderDetailId
    FROM orders o
    LEFT JOIN order_details od ON o.id = od.order_id
    LEFT JOIN merchants m ON od.merchant_id = m.id
    LEFT JOIN order_items i ON od.id = i.order_detail_id
    WHERE 
      (:merchantId IS NULL OR m.id = :merchantId)
      AND (:name IS NULL OR m.name ILIKE CONCAT('%', :name, '%') OR i.name ILIKE CONCAT('%', :name, '%'))
      AND (:merchantCategory IS NULL OR m.category = :merchantCategory)
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findOrdersWithDetails(
        @Param("merchantId") Long merchantId,
        @Param("name") String name,
        @Param("merchantCategory") String merchantCategory,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
}
