package com.leafall.yourtaxi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class PointKey implements Serializable {
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    @Column(name = "index", nullable = false)
    private Integer index;
}
