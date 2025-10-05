package com.kopi.belimang.order.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kopi.belimang.core.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = """
        UPDATE orders
        SET created_flag = true
        WHERE id = :orderId AND has_order_made = false
        RETURNING *
        """, nativeQuery = true)
    public Order updateHasOrderMadeFlag(@Param("orderId") Long orderId);
}
