package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.dto.user.*;
import com.leafall.yourtaxi.exceptions.BadRequestException;
import com.leafall.yourtaxi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/v1/users/current")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        var user = service.getCurrentUser();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    @PostMapping("/v1/users/signin")
    public ResponseEntity<SuccessAuthDto> signIn(@RequestBody SignInDto dto) {
        var user = service.signIn(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/signup")
    public ResponseEntity<SuccessAuthDto> signUp(@RequestBody SignUpDto dto) {
        var user = service.signUp(dto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/v1/users/verify")
    public ResponseEntity<SuccessAuthDto> verify(@RequestBody VerifyEmailDto dto) {
        var user = service.verifyEmail(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/tokens/refresh")
    public ResponseEntity<TokenHolder> refresh(@RequestBody RefreshDto dto) {
        var user = service.refresh(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/logout")
    public ResponseEntity<TokenHolder> logout(HttpServletRequest request) {
        var authToken = request.getHeader("Authorization");
        if (!authToken.startsWith("Bearer ")) {
            throw new BadRequestException("base.error.bad-request");
        }
        authToken = authToken.substring(7);

        service.logout(authToken);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
