package com.leafall.yourtaxi.entity;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.listener.TimestampListener;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Table(name = "feedbacks")
@Entity
@EntityListeners(value = {TimestampListener.class})
@Data
public class FeedbackEntity implements CreatedAtTimestampAware {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "phone")
    private String phone;

    @Column(name = "read_at")
    private Long readAt;

    @ManyToOne
    @JoinColumn(name = "read_by")
    private UserEntity readBy;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;
}
