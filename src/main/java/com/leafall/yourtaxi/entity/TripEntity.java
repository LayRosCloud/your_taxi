package com.leafall.yourtaxi.entity;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.listener.TimestampListener;
import jakarta.persistence.*;
import lombok.Data;

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

    @Column(name = "end_at")
    private Long endAt;
}
