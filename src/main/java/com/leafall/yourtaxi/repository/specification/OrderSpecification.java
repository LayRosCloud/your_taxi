package com.leafall.yourtaxi.repository.specification;

import com.leafall.yourtaxi.entity.OrderEntity;
import com.leafall.yourtaxi.entity.enums.OrderPaymentType;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.UUID;

public class OrderSpecification {
    public static Specification<OrderEntity> hasClientId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return null;
            }
            return cb.equal(root.join("user").get("id"), userId);
        };
    }

    public static Specification<OrderEntity> hasDriver(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return null;
            }
            return cb.equal(root.join("executor").join("user").get("id"), userId);
        };
    }

    public static Specification<OrderEntity> hasIsBigDistance(Boolean isBigDistance) {
        return (root, query, cb) -> {
            if (isBigDistance == null) {
                return null;
            }
            return cb.equal(root.get("isBigDistance"), isBigDistance);
        };
    }

    public static Specification<OrderEntity> hasPaymentType(OrderPaymentType paymentType) {
        return (root, query, cb) -> {
            if (paymentType == null) {
                return null;
            }
            return cb.equal(root.get("paymentType"), paymentType);
        };
    }

    public static Specification<OrderEntity> hasStatus(OrderStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<OrderEntity> betweenPrice(Double priceFrom, Double priceTo) {
        return (root, query, cb) -> {
            if (priceFrom != null && priceTo == null) {
                return cb.greaterThanOrEqualTo(root.get("price"), priceFrom);
            } else if(priceFrom == null && priceTo != null) {
                return cb.lessThanOrEqualTo(root.get("price"), priceTo);
            } else if (priceFrom != null && priceTo != null) {
                return cb.between(root.get("price"), priceFrom, priceTo);
            }
            return null;
        };
    }

    public static Specification<OrderEntity> betweenDate(Date dateFrom, Date dateTo) {
        return (root, query, cb) -> {
            if (dateFrom != null && dateTo == null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.getTime());
            } else if(dateFrom == null && dateTo != null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), dateTo.getTime());
            } else if (dateFrom != null && dateTo != null) {
                return cb.between(root.get("createdAt"),  dateFrom.getTime(), dateTo.getTime());
            }
            return null;
        };
    }
}
