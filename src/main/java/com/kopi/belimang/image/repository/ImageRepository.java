package com.kopi.belimang.image.repository;

import com.kopi.belimang.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO images (url, created_at, updated_at)
        VALUES (:id, :url, :createdAt, :updatedAt)
        """, nativeQuery = true)
    void create(
            @Param("url") String url,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt
    );
}
