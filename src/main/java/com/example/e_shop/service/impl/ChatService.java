package com.example.e_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.e_shop.model.entity.Conversations;
import com.example.e_shop.model.entity.Messages;
import com.example.e_shop.mapper.ConversationsMapper;
import com.example.e_shop.mapper.MessagesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@CacheConfig(cacheNames = "conversationCache")
@Service
public class ChatService extends ServiceImpl<ConversationsMapper, Conversations> {

    @Autowired
    private ConversationsMapper conversationMapper;

    @Autowired
    private MessagesMapper messageMapper;

    @Autowired
    private MessageCacheService messageCacheService;

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

    // 获取对话消息列表（合并 DB + Redis 缓存）
    public List<Messages> getConversationMessages(Long currentUserId, Long otherUserId) {
        // 获取或创建会话
        Conversations conversation = getOrCreateConversation(currentUserId, otherUserId);
        Long convId = conversation.getId();

        // 从 DB 读取已落库的消息
        List<Messages> dbMessages = messageMapper.getConversationMessages(convId, currentUserId);
        // 从 Redis 读取待落库的消息
        List<Messages> pendingMessages = messageCacheService.getPendingMessages(convId);

        // 合并并按发送时间排序
        return Stream.concat(dbMessages.stream(), pendingMessages.stream())
                .sorted(Comparator.comparing(Messages::getSentAt))
                .toList();
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

        messageCacheService.cacheMessage(message); // 写入Redis缓存，定时任务落库

        // 更新会话的 last_message（直接写DB，保证会话列表实时更新）
        conversation.setLastMessage(content.length() > 100 ? content.substring(0, 97) + "..." : content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        updateById(conversation);

        return message;
    }

    // 获取用户所有未读消息总数
    @Cacheable(key = "T(com.example.e_shop.util.CacheKeyUtil).getMessageAmountKey(#userId)")
    public long countAllUnreadMessages(Long userId) {
        return messageMapper.selectCount(
            new LambdaQueryWrapper<Messages>()
                .eq(Messages::getReceiverUserId, userId)
                .eq(Messages::getIsRead, false)
                .eq(Messages::getIsDeletedReceiver, false)
        );
    }
    @Cacheable(key = "T(com.example.e_shop.util.CacheKeyUtil).getCountUnreadByConversationKey(#userId)")
    // 获取每个对话的未读消息数
    public List<Map<String, Object>> countUnreadByConversation(Long userId) {
        QueryWrapper<Messages> wrapper = new QueryWrapper<>();
        wrapper.select("conversation_id", "COUNT(*) as unread_count")
                .eq("receiver_user_id", userId)
                .eq("is_read", false)
                .eq("is_deleted_receiver", false)
                .groupBy("conversation_id");
        return messageMapper.selectMaps(wrapper);
    }

    // 标记消息为已读（DB + Redis缓存同步更新，同时清除未读数缓存）
    @Caching(evict = {
            @CacheEvict(cacheNames = "conversationCache", key = "T(com.example.e_shop.util.CacheKeyUtil).getMessageAmountKey(#currentUserId)"),
            @CacheEvict(cacheNames = "conversationCache", key = "T(com.example.e_shop.util.CacheKeyUtil).getCountUnreadByConversationKey(#currentUserId)")
    })
    @Transactional
    public void markMessagesAsRead(Long conversationId, Long currentUserId) {
        // 更新 DB 中未读消息
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getConversationId, conversationId)
                .eq(Messages::getReceiverUserId, currentUserId)
                .eq(Messages::getIsRead, false);

        Messages message = new Messages();
        message.setIsRead(true);
        messageMapper.update(message, wrapper);

        // 更新 Redis 中待落库消息的已读状态
        messageCacheService.markPendingAsRead(conversationId, currentUserId);
    }

}