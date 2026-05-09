package com.example.e_shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Messages {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("conversation_id")
    private Long conversationId;

    @TableField("sender_user_id")
    private Long senderUserId;

    @TableField("receiver_user_id")
    private Long receiverUserId;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("is_deleted_sender")
    private Boolean isDeletedSender;

    @TableField("is_deleted_receiver")
    private Boolean isDeletedReceiver;

    @TableField("reply_to_message_id")
    private Long replyToMessageId;

    @TableField("sent_at")
    private LocalDateTime sentAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}