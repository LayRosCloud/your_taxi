package com.leafall.yourtaxi.entity;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.aware.UpdatedAtTimestampAware;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.entity.listener.TimestampListener;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@EntityListeners({TimestampListener.class})
public class UserEntity implements CreatedAtTimestampAware, UpdatedAtTimestampAware {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fullname", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false, columnDefinition = "users_role_enum")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private UserRole role;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "deleted_at")
    private Long deletedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserInfoEntity info;
}
