package com.leafall.yourtaxi.entity;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.enums.GeneratedFileStatus;
import com.leafall.yourtaxi.entity.listener.TimestampListener;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Table(name = "generated_files")
@Entity
@Data
@EntityListeners(value = { TimestampListener.class })
public class GeneratedFileEntity implements CreatedAtTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "status", nullable = false, columnDefinition = "orders_status_enum")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private GeneratedFileStatus status;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity user;

    @Column(name = "created_at")
    private Long createdAt;
}
