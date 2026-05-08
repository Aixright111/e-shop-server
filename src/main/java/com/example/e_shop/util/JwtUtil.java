package com.example.e_shop.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtil {

    // 密钥
    private static final String SECRET_KEY = "VIBE_MUSIC"; // 更改为你的密钥
    // 设置 JWT 的过期时间 6 小时
    private static final long EXPIRATION_TIME1 = 1000 * 60 * 60 * 6;
    private static final long EXPIRATION_TIME2 = 1000 * 60 * 60 * 12;
    /**
     * 生成 JWT token
     *
     * @param claims 自定义的业务数据
     * @return JWT token
     */
    public static String generateToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims) // 自定义的业务数据
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME1)) // 设置过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用 HMAC256 算法加密
    }
    public static  String generateRefreshToken(Map<String, Object> claims){
        return JWT.create()
                .withClaim("claims", claims) // 自定义的业务数据
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME2)) // 设置过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用 HMAC256 算法加密

    }

    /**
     * 解析 JWT token
     *
     * @param token JWT token
     * @return 自定义的业务数据
     */
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}
