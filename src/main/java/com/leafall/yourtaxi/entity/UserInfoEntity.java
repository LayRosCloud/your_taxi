package com.leafall.yourtaxi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.util.UUID;

@Entity
@Table(name = "users_info")
@Data
@ToString
public class UserInfoEntity {
    @Id
    private UUID id;

    @JoinColumn(name = "user_id")
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private UserEntity user;

    @Column(name = "phone", nullable = false)
    private String phone;
}
