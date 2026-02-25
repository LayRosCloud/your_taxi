package com.leafall.yourtaxi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "points")
@Data
public class PointEntity {

    @EmbeddedId
    private PointKey id;

    @JoinColumn(name = "order_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    private OrderEntity order;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "point", nullable = false)
    private Geometry point;

    public Integer getIndex() {
        return id.getIndex();
    }
}
