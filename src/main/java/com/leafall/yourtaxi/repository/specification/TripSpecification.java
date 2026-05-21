package com.leafall.yourtaxi.repository.specification;

import com.leafall.yourtaxi.entity.TripEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.UUID;

public class TripSpecification {
    public static Specification<TripEntity> betweenDates(Date dateFrom, Date dateTo) {
        return (root, query, criteriaBuilder) -> {
            if (dateFrom == null && dateTo == null) {
                return null;
            }
            if (dateFrom != null && dateTo == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("endAt"), dateFrom.getTime());
            }
            if (dateFrom == null && dateTo != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("endAt"), dateTo.getTime());
            }
            return criteriaBuilder.between(root.get("endAt"), dateFrom.getTime(), dateTo.getTime());
        };
    }

    public static Specification<TripEntity> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null) {
                return null;
            }
            var concat = search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("user").get("fullName")), concat),
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("car").get("number")), concat),
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("car").get("mark")), concat)
            );
        };
    }

    public static Specification<TripEntity> equalsEmployeeId(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return null;
            }
            return criteriaBuilder.equal(
                    root.join("user").get("id"), id
            );
        };
    }
}
