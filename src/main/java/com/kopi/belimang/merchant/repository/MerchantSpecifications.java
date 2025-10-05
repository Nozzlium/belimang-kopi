package com.kopi.belimang.merchant.repository;

import org.springframework.data.jpa.domain.Specification;

import com.kopi.belimang.core.entities.Merchant;

public class MerchantSpecifications {
    public static Specification<Merchant> hasPoint(Float lat, Float lon) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("location"), criteriaBuilder.function(
                    "ST_SetSRID",
                    Object.class,
                    criteriaBuilder.function(
                            "ST_MakePoint",
                            Object.class,
                            criteriaBuilder.literal(lon),
                            criteriaBuilder.literal(lat)
                    ),
                    criteriaBuilder.literal(4326)
            ));
        };
    }
    public static Specification<Merchant> hasId(String idString) {
        return (root, query, criteriaBuilder) -> {
            long id;
            try {
                id = Long.parseLong(idString);
                if (id < 0) {
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
            return criteriaBuilder.equal(root.get("id"), idString);
        };
    }

    public static Specification<Merchant> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Merchant> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null || category.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("merchantCategory"), category);
        };
    }
}
