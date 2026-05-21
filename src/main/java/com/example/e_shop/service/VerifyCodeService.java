package com.example.e_shop.service;

/**
 * 邮箱验证码服务接口。
 */
public interface VerifyCodeService {

    /**
     * 生成验证码并存入 Redis，同时发送到指定邮箱。
     *
     * @param email 目标邮箱地址
     */
    void sendCode(String email);
}
