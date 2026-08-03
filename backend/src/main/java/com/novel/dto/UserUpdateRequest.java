package com.novel.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String nickname;
    private String email;
    private String avatar;
}
