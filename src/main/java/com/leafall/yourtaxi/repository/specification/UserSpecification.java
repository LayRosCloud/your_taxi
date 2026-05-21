package com.leafall.yourtaxi.repository.specification;

import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<UserEntity> findBySearch(String search) {
        return (root, query, cb) -> {
            if (search == null) {
                return null;
            }
            var concat = search.toLowerCase() +
                    "%";
            return cb.or(cb.like(cb.lower(root.get("fullName")), concat),
                    cb.like(cb.lower(root.get("email")), concat));
        };
    }

    public static Specification<UserEntity> equalsRole(UserRole role) {
        return (root, query, cb) -> {
            if (role == null) {
                return null;
            }
            return cb.equal(root.get("role"), role);
        };
    }
    public static Specification<UserEntity> isNullDeletedAt(Boolean isNull) {
        return (root, query, cb) -> {
            if (isNull == null) {
                return null;
            }
            if (isNull) {
                return cb.isNull(root.get("deletedAt"));
            }
            return cb.isNotNull(root.get("deletedAt"));
        };
    }

}
