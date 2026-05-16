package com.example.e_shop.model.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private String name;
    private String email;
    private Long id;
    private LocalDateTime createdAt;
    private String avatarUrl;
}
