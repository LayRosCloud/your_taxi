package com.leafall.yourtaxi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users_info")
@Data
public class UserInfoEntity {
    @Id
    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "phone", nullable = false)
    private String phone;

}
