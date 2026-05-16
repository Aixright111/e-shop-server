package com.example.e_shop.model.DTO;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long receiverId;
    private String content;
}