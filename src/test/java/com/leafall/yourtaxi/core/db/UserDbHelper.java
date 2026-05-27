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
        var generated = UserEntityUtils.generateUser(UserRole.USER);
        return userRepository.save(generated);
    }

    public UserEntity save(UserRole role) {
        var generated = UserEntityUtils.generateUser(role);
        return userRepository.save(generated);
    }
}
