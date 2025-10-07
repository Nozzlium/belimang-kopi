package com.kopi.belimang.merchant.repository;

import com.kopi.belimang.core.entities.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    @Query(value = """
        SELECT * FROM merchants
        WHERE (:merchantId IS NULL OR id = :merchantId)
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
    List<Merchant> findMerchantsWithFilters(
            @Param("merchantId") Long merchantId,
            @Param("name") String name,
            @Param("category") String category,
            @Param("sortOrder") String sortOrder,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    @Query(value = """
        SELECT COUNT(1) FROM merchants
        WHERE (:merchantId IS NULL OR id = :merchantId)
        AND (:name IS NULL OR name ILIKE CONCAT('%', :name, '%'))
        AND (:category IS NULL OR category = :category)
        """,
            nativeQuery = true)
    long countWithFilters(
            @Param("merchantId") Long merchantId,
            @Param("name") String name,
            @Param("category") String category
    );

    @Query(value = """
        SELECT 
            m.id, m.name, m.category, m.image_url, ST_Y(m.location::geometry) AS lat, ST_X(m.location::geometry) AS lon, m.created_at,
            mi.id AS item_id, mi.name AS item_name, mi.category, mi.price, mi.image_url AS item_image_url, mi.created_at AS item_created_at,
            ST_Distance(m.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)) AS distance
        FROM
            merchants m
        LEFT JOIN merchant_items mi ON mi.merchant_id = m.id
        WHERE
            (:merchantId IS NULL OR m.id = :merchantId) AND
            (m.name ILIKE CONCAT('%', :name, '%')) AND
            (:category IS NULL OR m.category = :category)
        ORDER BY
            distance ASC
        LIMIT :lmt OFFSET :offs
    """, nativeQuery = true
    )
    List<Object[]> findNearbyMerchants(
        @Param("lat") Double lat,
        @Param("lon") Double lon,
        @Param("merchantId") Long merchantId,
        @Param("name") String name,
        @Param("category") String category,
        @Param("lmt") int lmt,
        @Param("offs") int offs
    );

    @Query("SELECT m FROM Merchant m WHERE m.id IN :ids")
    List<Merchant> findByIdIn(List<Long> ids);

    @Query(value = """
    SELECT m1.id AS id1, m2.id AS id2,
           ST_Distance(m1.location, m2.location) AS distance
    FROM merchants m1
    JOIN merchants m2 ON m1.id <> m2.id
    WHERE m1.id IN (:ids) AND m2.id IN (:ids)
    """, nativeQuery = true)
    List<Object[]> findPairwiseDistances(@Param("ids") List<Long> ids);
}
