package com.leafall.yourtaxi.entity;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.enums.TripStatus;
import com.leafall.yourtaxi.entity.listener.TimestampListener;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Data
@Entity
@EntityListeners({TimestampListener.class})
@Table(name = "trips")
public class TripEntity implements CreatedAtTimestampAware {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private UserEntity user;

    @JoinColumn(name = "car_id")
    @ManyToOne
    private CarEntity car;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "status", nullable = false, columnDefinition = "trips_status_enum")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TripStatus status;

    @Column(name = "end_at")
    private Long endAt;
}
