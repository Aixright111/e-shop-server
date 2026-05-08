package com.example.e_shop.VO;

import com.example.e_shop.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private String name;
    private String email;
    private Long id;
    private LocalDateTime createdAt;
}
