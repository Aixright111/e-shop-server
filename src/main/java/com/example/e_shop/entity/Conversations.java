package com.example.e_shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversations")
public class Conversations {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "participant_user_ids", typeHandler = com.example.e_shop.handler.LongArrayTypeHandler.class)
    private Long[] participantUserIds;

    @TableField("last_message")
    private String lastMessage;

    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
