package com.novel.controller;

import com.novel.common.Result;
import com.novel.dto.LoginRequest;
import com.novel.dto.LoginResponse;
import com.novel.dto.RegisterRequest;
import com.novel.dto.UserUpdateRequest;
import com.novel.entity.User;
import com.novel.interceptor.JwtInterceptor;
import com.novel.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_ATTR);
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(HttpServletRequest request,
                                       @RequestBody UserUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_ATTR);
        userService.updateUserInfo(userId, updateRequest);
        return Result.success();
    }
}
