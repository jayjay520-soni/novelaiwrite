package com.novel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.novel.dto.LoginRequest;
import com.novel.dto.LoginResponse;
import com.novel.dto.RegisterRequest;
import com.novel.dto.UserUpdateRequest;
import com.novel.entity.User;

public interface UserService extends IService<User> {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    User getUserInfo(Long userId);

    void updateUserInfo(Long userId, UserUpdateRequest request);
}
