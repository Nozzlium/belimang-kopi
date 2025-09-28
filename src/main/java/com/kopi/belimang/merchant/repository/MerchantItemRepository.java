package com.kopi.belimang.merchant.repository;

import com.kopi.belimang.core.entities.Merchant;
import com.kopi.belimang.core.entities.MerchantItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantItemRepository extends JpaRepository<MerchantItem, Long> {

    @Query(value = """
        SELECT * FROM merchant_items
        WHERE (:merchantId IS NULL OR merchant_id = :merchantId)
        AND (:name IS NULL OR name ILIKE CONCAT('%', :name, '%'))
        AND (:category IS NULL OR category = :category)
        ORDER BY 
        CASE 
            WHEN :sortOrder = 'asc' THEN created_at 
        END ASC,
        CASE 
            WHEN :sortOrder = 'desc' THEN created_at 
        END DESC
        LIMIT :limit OFFSET :offset
        """,
            nativeQuery = true)
    List<MerchantItem> findMerchantItemsWithFilters(
            @Param("merchantId") Long merchantId,
            @Param("name") String name,
            @Param("category") String category,
            @Param("sortOrder") String sortOrder,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
        SELECT COUNT(1) FROM merchant_items
        WHERE (:merchantId IS NULL OR merchant_id = :merchantId)
        AND (:name IS NULL OR name ILIKE CONCAT('%', :name, '%'))
        AND (:category IS NULL OR category = :category)
        """,
            nativeQuery = true)
    long countWithFilters(
            @Param("merchantId") Long merchantId,
            @Param("name") String name,
            @Param("category") String category
    );
}
