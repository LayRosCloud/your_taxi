package com.leafall.yourtaxi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "users_info")
@Data
public class UserInfoEntity {
    @Id
    private UUID id;

    @JoinColumn(name = "user_id")
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "phone", nullable = false)
    private String phone;
}
