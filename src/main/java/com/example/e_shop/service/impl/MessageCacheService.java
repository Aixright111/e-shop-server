package com.example.e_shop.service.impl;

import com.example.e_shop.model.entity.Messages;
import com.example.e_shop.mapper.MessagesMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageCacheService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MessagesMapper messageMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PENDING_QUEUE = "msg:pending";          // 待落库消息队列（List）
    private static final String CONV_CACHE = "msg:conv:%d";             // 每个会话的待落库消息（List）

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 缓存消息：写入 Redis，等待定时任务落库
     */
    public void cacheMessage(Messages msg) {
        try {
            String json = objectMapper.writeValueAsString(msg);
            // 加入全局待落库队列（给调度器用）
            stringRedisTemplate.opsForList().rightPush(PENDING_QUEUE, json);
            // 加入会话缓存（给即时读取用）
            stringRedisTemplate.opsForList().rightPush(String.format(CONV_CACHE, msg.getConversationId()), json);
            log.debug("消息已缓存: convId={}", msg.getConversationId());
        } catch (JsonProcessingException e) {
            log.error("消息序列化失败", e);
            throw new RuntimeException("消息序列化失败", e);
        }
    }

    /**
     * 获取某个会话待落库的缓存消息
     */
    public List<Messages> getPendingMessages(Long convId) {
        List<String> jsons = stringRedisTemplate.opsForList().range(String.format(CONV_CACHE, convId), 0, -1);
        if (jsons == null || jsons.isEmpty()) return Collections.emptyList();
        return jsons.stream().map(this::parseMessage).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 将会话中待落库消息标记为已读
     */
    public void markPendingAsRead(Long convId, Long userId) {
        String key = String.format(CONV_CACHE, convId);
        List<String> jsons = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (jsons == null || jsons.isEmpty()) return;

        boolean changed = false;
        List<String> updated = new ArrayList<>();
        for (String json : jsons) {
            Messages msg = parseMessage(json);
            if (msg != null && msg.getReceiverUserId().equals(userId) && !msg.getIsRead()) {
                msg.setIsRead(true);
                try {
                    updated.add(objectMapper.writeValueAsString(msg));
                    changed = true;
                } catch (JsonProcessingException e) {
                    updated.add(json);
                }
            } else {
                updated.add(json);
            }
        }
        if (changed) {
            stringRedisTemplate.delete(key);
            if (!updated.isEmpty()) {
                stringRedisTemplate.opsForList().rightPushAll(key, updated);
            }
        }
    }

    /**
     * 批量落库：从待落库队列取出并写入数据库
     */
    public void flushToDatabase() {
        // 一次最多取 50 条
        List<String> jsons = stringRedisTemplate.opsForList().leftPop(PENDING_QUEUE, 50);
        if (jsons == null || jsons.isEmpty()) return;

        Set<Long> convIds = new HashSet<>();
        List<Messages> messages = new ArrayList<>();

        for (String json : jsons) {
            Messages msg = parseMessage(json);
            if (msg == null) continue;
            messages.add(msg);
            convIds.add(msg.getConversationId());
        }

        if (messages.isEmpty()) return;

        // 批量插入数据库
        for (Messages msg : messages) {
            messageMapper.insert(msg);
        }
        log.info("消息落库 {} 条, 涉及会话: {}", messages.size(), convIds);

        // 清除这些会话的缓存，下次读取时从 DB 获取
        for (Long convId : convIds) {
            stringRedisTemplate.delete(String.format(CONV_CACHE, convId));
        }
    }

    private Messages parseMessage(String json) {
        try {
            return objectMapper.readValue(json, Messages.class);
        } catch (JsonProcessingException e) {
            log.error("消息反序列化失败", e);
            return null;
        }
    }
}
