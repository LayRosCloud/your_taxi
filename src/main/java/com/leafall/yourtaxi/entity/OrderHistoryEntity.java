package com.leafall.yourtaxi.entity;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import com.leafall.yourtaxi.entity.listener.TimestampListener;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.locationtech.jts.geom.Geometry;

import java.util.UUID;

@Data
@Entity
@Table(name = "orders_history")
@EntityListeners({TimestampListener.class})
public class OrderHistoryEntity implements CreatedAtTimestampAware {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "message")
    private String message;

    @Column(name = "status", nullable = false, columnDefinition = "orders_status_enum")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private OrderStatus status;

    @Column(name = "point", nullable = true)
    private Geometry point;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;
}
