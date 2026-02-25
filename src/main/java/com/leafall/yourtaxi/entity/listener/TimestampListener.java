package com.leafall.yourtaxi.entity.listener;

import com.leafall.yourtaxi.entity.aware.CreatedAtTimestampAware;
import com.leafall.yourtaxi.entity.aware.UpdatedAtTimestampAware;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TimestampListener {

    @PrePersist
    private void beforeSave(Object object) {
        var now = Instant.now().toEpochMilli();
        if (object instanceof CreatedAtTimestampAware) {
            ((CreatedAtTimestampAware) object).setCreatedAt(now);
        }
        if (object instanceof UpdatedAtTimestampAware) {
            ((UpdatedAtTimestampAware) object).setUpdatedAt(now);
        }
    }

    @PreUpdate
    private void beforeUpdate(Object object) {
        var now = Instant.now().toEpochMilli();
        if (object instanceof UpdatedAtTimestampAware) {
            ((UpdatedAtTimestampAware) object).setUpdatedAt(now);
        }
    }
}
