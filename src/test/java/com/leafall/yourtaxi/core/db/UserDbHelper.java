package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.core.utils.entity.UserEntityUtils;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDbHelper {
    private final UserRepository userRepository;

    public UserEntity save() {
        var generated = UserEntityUtils.generateUser(UserRole.USER, true);
        return userRepository.save(generated);
    }

    public UserEntity save(UserRole role) {
        var generated = UserEntityUtils.generateUser(role, true);
        return userRepository.save(generated);
    }

    public UserEntity save(UserRole role, Boolean isActive) {
        var generated = UserEntityUtils.generateUser(role, isActive);
        return userRepository.save(generated);
    }

    public UserEntity save(UserRole role, Boolean isActive, Long deletedAt) {
        var generated = UserEntityUtils.generateUser(role, isActive);
        generated.setDeletedAt(deletedAt);
        return userRepository.save(generated);
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }
}
