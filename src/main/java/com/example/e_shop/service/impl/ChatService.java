package com.example.e_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.e_shop.model.entity.Conversations;
import com.example.e_shop.model.entity.Messages;
import com.example.e_shop.mapper.ConversationsMapper;
import com.example.e_shop.mapper.MessagesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@CacheConfig(cacheNames = "conversationCache")
@Service
public class ChatService extends ServiceImpl<ConversationsMapper, Conversations> {

    @Autowired
    private ConversationsMapper conversationMapper;

    @Autowired
    private MessagesMapper messageMapper;

    // 获取或创建会话
    @Transactional
    public Conversations getOrCreateConversation(Long currentUserId, Long otherUserId) {
        // 查询已存在会话
        Conversations conversation = conversationMapper.findByParticipants(currentUserId, otherUserId);

        if (conversation != null) {
            return conversation;
        }

        // 创建新会话
        conversation = new Conversations();
        conversation.setParticipantUserIds(new Long[]{currentUserId, otherUserId});
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        save(conversation);  // MyBatis-Plus 的 save 方法
        return conversation;
    }

    // 获取对话消息列表
    public List<Messages> getConversationMessages(Long currentUserId, Long otherUserId) {
        // 获取或创建会话
        Conversations conversation = getOrCreateConversation(currentUserId, otherUserId);

        // 查询消息
        return messageMapper.getConversationMessages(conversation.getId(), currentUserId);
    }

    // 发送消息
    @Transactional
    public Messages sendMessage(Long senderId, Long receiverId, String content) {
        // 获取或创建会话
        Conversations conversation = getOrCreateConversation(senderId, receiverId);

        // 创建消息
        Messages message = new Messages();
        message.setConversationId(conversation.getId());
        message.setSenderUserId(senderId);
        message.setReceiverUserId(receiverId);
        message.setContent(content);
        message.setIsRead(false);
        message.setIsDeletedSender(false);
        message.setIsDeletedReceiver(false);
        message.setSentAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        messageMapper.insert(message);

        // 更新会话的 last_message
        conversation.setLastMessage(content.length() > 100 ? content.substring(0, 97) + "..." : content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        updateById(conversation);

        return message;
    }

    // 标记消息为已读
    @Transactional
    public void markMessagesAsRead(Long conversationId, Long currentUserId) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getConversationId, conversationId)
                .eq(Messages::getReceiverUserId, currentUserId)
                .eq(Messages::getIsRead, false);

        Messages message = new Messages();
        message.setIsRead(true);
        messageMapper.update(message, wrapper);
    }
}