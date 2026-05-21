package com.example.e_shop.service.impl;

import com.example.e_shop.service.VerifyCodeService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现。
 * <p>
 * 生成 6 位随机验证码，存入 Redis（5 分钟过期），并通过 QQ 邮箱发送。
 * </p>
 */
@Slf4j
@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    /** Redis 中验证码的过期时间（分钟） */
    private static final long CODE_TTL_MINUTES = 5;

    /** 验证码在 Redis 中的 key 前缀 */
    private static final String REDIS_KEY_PREFIX = "verify_code:";

    /** 发件人邮箱，从配置中读取 */
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.protocol}")
    private String mailProtocol;

    /** 随机数生成器 */
    private static final SecureRandom RANDOM = new SecureRandom();

    @PostConstruct
    public void init() {
        log.debug("邮件服务初始化配置: host={}, port={}, protocol={}, from={}",
                mailHost, mailPort, mailProtocol, fromEmail);
    }

    @Override
    public void sendCode(String email) {
        log.debug("收到发送验证码请求: email={}", email);
        if (email == null || email.isBlank()) {
            log.warn("邮箱地址为空，拒绝发送");
            throw new IllegalArgumentException("邮箱地址不能为空");
        }

        // 1. 生成 6 位随机验证码
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        log.debug("验证码已生成: email={}, code={}", email, code);

        // 2. 存入 Redis（5 分钟过期）
        String redisKey = REDIS_KEY_PREFIX + email;
        Instant redisStart = Instant.now();
        stringRedisTemplate.opsForValue().set(redisKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        Duration redisCost = Duration.between(redisStart, Instant.now());
        log.info("验证码已存入 Redis: key={}, email={}, code={}, TTL={}分钟, 耗时={}ms",
                redisKey, email, code, CODE_TTL_MINUTES, redisCost.toMillis());

        // 3. 发送邮件
        Instant mailStart = Instant.now();
        try {
            log.debug("开始构造邮件: from={}, to={}, subject=e-shop 验证码", fromEmail, email);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("e-shop 验证码");
            String htmlContent = "您的验证码为：<b>" + code + "</b>，有效期 " + CODE_TTL_MINUTES + " 分钟。";
            helper.setText(htmlContent, true);
            log.debug("邮件构造完成，开始通过 SMTP 发送: host={}, port={}", mailHost, mailPort);

            mailSender.send(message);

            Duration mailCost = Duration.between(mailStart, Instant.now());
            log.info("验证码邮件发送成功: email={}, 耗时={}ms, 发件人={}, 收件人={}",
                    email, mailCost.toMillis(), fromEmail, email);
        } catch (MessagingException e) {
            Duration mailCost = Duration.between(mailStart, Instant.now());
            log.error("验证码邮件发送失败: email={}, 耗时={}ms, 异常类型={}, 异常信息={}",
                    email, mailCost.toMillis(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}
