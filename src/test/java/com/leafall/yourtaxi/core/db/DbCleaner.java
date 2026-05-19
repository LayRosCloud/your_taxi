package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbCleaner {

    private final UserRepository repository;

    public void clear() {
        repository.deleteAll();
    }
}
