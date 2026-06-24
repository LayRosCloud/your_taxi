package com.leafall.yourtaxi.core.utils.equals;

import com.leafall.yourtaxi.dto.user.*;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;

import static org.junit.jupiter.api.Assertions.*;

public class UserEqualsUtils {
    public static void equalsUserSignUp(SuccessAuthDto response, SignUpDto userDto) {
        assertNull(response.getTokens());
        assertNotNull(response.getUser());
        assertEquals(userDto.getEmail(), response.getUser().getEmail());
        assertEquals(userDto.getFullName(), response.getUser().getFullName());
        assertEquals(Boolean.FALSE,response.getUser().getIsActive());
        assertEquals(UserRole.USER.getAuthority(), response.getUser().getRole());
        assertNull(response.getUser().getInfo());
    }

    public static void equalsUser(SuccessAuthDto response, UserEntity user) {
        assertNull(response.getTokens());
        equalsUsers(response.getUser(), user);
    }

    public static void equalsUserAndTokens(SuccessAuthDto response, UserEntity user) {
        assertNotNull(response.getTokens());
        assertNotNull(response.getTokens().getAccessToken());
        assertNotNull(response.getTokens().getRefreshToken());
        equalsUsers(response.getUser(), user);
    }

    public static void equalsUsers(UserResponseDto response, UserEntity user) {
        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFullName(), response.getFullName());
        assertEquals(user.getIsActive(),response.getIsActive());
        assertEquals(user.getAvatar(), response.getAvatar());
        assertEquals(user.getRole().name(), response.getRole());
        assertNull(response.getInfo());
    }

    public static void equalsUsers(UserDetailResponseDto response, UserEntity user) {
        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFullName(), response.getFullName());
        assertEquals(user.getIsActive(),response.getIsActive());
        assertEquals(user.getAvatar(), response.getAvatar());
        assertEquals(user.getRole().name(), response.getRole());
        assertNull(response.getInfo());
    }

    public static void equalsUserSignUpDeletedAccount(SuccessAuthDto response, SignUpDto userDto) {
        assertNotNull(response.getTokens());
        assertNotNull(response.getTokens().getAccessToken());
        assertNotNull(response.getTokens().getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(userDto.getEmail(), response.getUser().getEmail());
        assertEquals(userDto.getFullName(), response.getUser().getFullName());
        assertEquals(Boolean.TRUE,response.getUser().getIsActive());
        assertEquals(UserRole.USER.getAuthority(), response.getUser().getRole());
        assertNull(response.getUser().getInfo());
    }
}
