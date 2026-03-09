package com.leafall.yourtaxi.entity;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.aware.UpdatedAtTimestampAware;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import com.leafall.yourtaxi.entity.listener.TimestampListener;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@EntityListeners({TimestampListener.class})
public class OrderEntity implements CreatedAtTimestampAware, UpdatedAtTimestampAware {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private UserEntity user;

    @JoinColumn(name = "executor_id", nullable = true)
    @ManyToOne(fetch = FetchType.EAGER)
    private TripEntity executor;

    @Column(name = "status", nullable = false, columnDefinition = "orders_status_enum")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private OrderStatus status;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    @ToString.Exclude
    private List<PointEntity> points;
}
