package com.leafall.yourtaxi.dto.order;

import lombok.Data;

import java.util.Objects;

@Data
public class OrderRedisWaitingChildDto {
    private String id;
    private Long createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (OrderRedisWaitingChildDto) o;
        return that.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
