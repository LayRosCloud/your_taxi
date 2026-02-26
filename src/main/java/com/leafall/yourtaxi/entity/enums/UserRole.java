package com.leafall.yourtaxi.entity.enums;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    USER,
    EMPLOYEE,
    DISPATCHER;

    @Override
    public @Nullable String getAuthority() {
        return this.name();
    }
}
