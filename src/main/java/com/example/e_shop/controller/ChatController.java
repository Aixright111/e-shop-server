package com.example.e_shop.controller;

import com.example.e_shop.DTO.SendMessageRequest;
import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.entity.Conversations;
import com.example.e_shop.entity.Messages;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.ChatService;
import com.example.e_shop.service.ConversationsService;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private ConversationsService conversationsService;
    // 获取与某个用户的对话消息
    @GetMapping("/messages/{otherUserId}")
    public Result<List<Messages>> getMessagesWithUser(
            @PathVariable Long otherUserId
          ) {  // 从拦截器获取当前用户ID
        Map<String,Object>claims=ThreadLocalUtil.get();
         Object currentUserIdObj=claims.get(JwtClaimsConstant.USER_ID);
         Long currentUserId= TypeConversionUtil.toLong(currentUserIdObj);
        List<Messages> messages = chatService.getConversationMessages(currentUserId, otherUserId);
        return Result.success(messages);
    }

    // 发送消息
    @PostMapping("/send")
    public Result<Messages> sendMessage(
            @RequestBody SendMessageRequest request
            ) {
        Map<String,Object>claims=ThreadLocalUtil.get();
        Object currentUserIdObj=claims.get(JwtClaimsConstant.USER_ID);
        Long currentUserId= TypeConversionUtil.toLong(currentUserIdObj);
        Messages message = chatService.sendMessage(currentUserId, request.getReceiverId(), request.getContent());
        return Result.success(message);
    }

    // 标记消息已读
    @PutMapping("/read")
    public Result<Void> markAsRead(
            @RequestBody Long conversationId
            ) {
        Map<String,Object>claims=ThreadLocalUtil.get();
        Object currentUserIdObj=claims.get(JwtClaimsConstant.USER_ID);
        Long currentUserId= TypeConversionUtil.toLong(currentUserIdObj);
        chatService.markMessagesAsRead(conversationId, currentUserId);
        return Result.success();
    }
    @GetMapping("/conversations")
    public Result<List<Conversations>> list() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        List<Conversations> list = conversationsService.getConversationsByUserId(userId);
        return Result.success(list);
    }
}

