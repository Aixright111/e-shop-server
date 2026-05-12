package com.example.e_shop.Scheduler;

import com.example.e_shop.mapper.TransactionrecordsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TransactionExpiryScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionrecordsMapper transactionMapper;

    /**
     * 每分钟执行一次，标记过期订单
     */
    @Scheduled(fixedDelay = 20000)  // 20s标记过期订单
    @Transactional
    public void markExpiredTransactions() {
        // 在 Java 中获取当前时间
        LocalDateTime now = LocalDateTime.now();

        String sql = """
        UPDATE transactionrecords 
        SET is_expired = TRUE
        WHERE is_expired = FALSE 
          AND transactiondeadline < ?
        """;

        int updated = jdbcTemplate.update(sql, now);
        if (updated > 0) {
            log.info("标记了 {} 个订单为过期状态", updated);
        } else {
            log.info("订单无过期");
        }
    }
    @Scheduled(fixedDelay = 30000, initialDelay = 60000)  // 60秒首次删除过期订单 30s/次
    @Transactional
    public void deleteTransactions(){
        String sql = """
        DELETE FROM transactionrecords 
        WHERE is_expired = TRUE 
         OR is_reject = TRUE
        """;

        int updated = jdbcTemplate.update(sql);
        if (updated > 0) {
            log.info("删除了 {} 个订单", updated);
        } else {
            log.info("0个删除");
        }
    }
}