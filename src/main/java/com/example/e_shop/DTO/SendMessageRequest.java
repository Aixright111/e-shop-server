package com.example.e_shop.DTO;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long receiverId;
    private String content;
}