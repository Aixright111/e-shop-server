package com.example.e_shop.interceptor;


import com.example.e_shop.config.RolePermissionManager;
import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.constant.MessageConstant;
import com.example.e_shop.constant.PathConstant;
import com.example.e_shop.util.JwtUtil;
import com.example.e_shop.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RolePermissionManager rolePermissionManager;

    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8"); // 设置响应的Content-Type
        response.getWriter().write(message);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许 CORS 预检请求（OPTIONS 方法）直接通过
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true; // 直接放行，确保 CORS 预检请求不会被拦截
        }
        String path = request.getRequestURI();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Map<String,Object> claims = JwtUtil.parseToken(token);
            String userName=claims.get(JwtClaimsConstant.USERNAME).toString();

            String redisToken=stringRedisTemplate.opsForValue().get(userName);

            if(redisToken.equals(token))
            {Map<String,Object> claims1=new HashMap<>();
            claims1=JwtUtil.parseToken(token);
            ThreadLocalUtil.set(claims);
            }
        }
        // 获取 Spring 的 PathMatcher 实例
        PathMatcher pathMatcher = new AntPathMatcher();

        // 定义允许访问的路径
        List<String> allowedPaths = Arrays.asList(
                "/user/register",
                "/user/login",
                "/products/",
                "/products/list",
                "/products/details/{id}",
                "/product/**"
        );
        if(token==null||token.isEmpty()){
            // 检查路径是否匹配
            boolean isAllowedPath = allowedPaths.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));

            if(!isAllowedPath) {
                System.out.println("路径不匹配");
                return false;}

        }




        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
