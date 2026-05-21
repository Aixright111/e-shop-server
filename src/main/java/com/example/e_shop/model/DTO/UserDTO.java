package com.example.e_shop.model.DTO;

import lombok.Data;

@Data
public class UserDTO {
    private String email;
    private String username;
    private String password;
    private String token;
    private String avatarUrl;
    private String code; // 邮箱验证码
}
