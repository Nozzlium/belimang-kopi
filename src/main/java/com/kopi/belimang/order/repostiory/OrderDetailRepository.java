package com.kopi.belimang.order.repostiory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kopi.belimang.core.entities.Order;
import com.kopi.belimang.core.entities.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    
    @Query(value = """
        SELECT 
            o.*
        FROM order_details od
        LEFT JOIN merchants m ON od.merchant_id = m.id
        LEFT JOIN order_items oi ON od.id = oi.order_detail_id
        WHERE (:merchantId IS NULL OR m.id = :merchantId)
        AND (:name IS NULL OR m.name ILIKE CONCAT('%', :name, '%') OR oi.name ILIKE CONCAT('%', :name, '%'))
        AND (:merchantCategory IS NULL OR m.category = :merchantCategory)
        LIMIT :limit OFFSET :offset
        """,
        nativeQuery = true)
    public List<Order> findOrders(
        @Param("limit") int limit,
        @Param("offset") int offset,
        @Param("merchantId") Long merchantId,
        @Param("name") String name,
        @Param("merchantCategory") String merchantCategory
    );
}
