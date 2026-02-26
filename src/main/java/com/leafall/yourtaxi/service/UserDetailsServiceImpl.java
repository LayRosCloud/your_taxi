package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.entity.UserDetailsImpl;
import com.leafall.yourtaxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findById(UUID.fromString(username))
                .orElseThrow(() -> new UsernameNotFoundException("user.error.not-found"));
        return new UserDetailsImpl(user);
    }
}
