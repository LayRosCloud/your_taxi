package com.leafall.yourtaxi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "codes")
@Data
public class CodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "code")
    private String code;
}
