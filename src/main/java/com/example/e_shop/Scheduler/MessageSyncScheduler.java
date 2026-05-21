package com.example.e_shop.Scheduler;

import com.example.e_shop.service.impl.MessageCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 消息缓存落库定时任务：每秒检查一次，将 Redis 中的缓存消息写入数据库
 */
@Slf4j
@Component
public class MessageSyncScheduler {

    @Autowired
    private MessageCacheService messageCacheService;

    @Scheduled(fixedDelay = 3000)
    public void flushMessages() {
        messageCacheService.flushToDatabase();
    }
}
