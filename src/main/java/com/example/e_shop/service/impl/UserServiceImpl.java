package com.example.e_shop.service.impl;

import ch.qos.logback.core.util.MD5Util;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.e_shop.DTO.UserDTO;
import com.example.e_shop.VO.UserVO;
import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.constant.MessageConstant;
import com.example.e_shop.entity.User;
import com.example.e_shop.mapper.UserMapper;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.e_shop.util.JwtUtil;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-07
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    public Result register(UserDTO userDTO) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        String passwordMD5 = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());
        user.setPassword(passwordMD5);
        if (userMapper.insert(user) == 0)
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        else
            return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);

    }

    public Result<String> login(UserDTO userDTO) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email",userDTO.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.USER + "不存在");
        }
        String MD5password = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());
        if (user.getPassword() .equals(MD5password)) {
            Map<String,Object> claims=new HashMap<>();
            claims.put(JwtClaimsConstant.EMAIL,user.getEmail());
            claims.put(JwtClaimsConstant.USERNAME,user.getName());
            claims.put(JwtClaimsConstant.USER_ID,user.getId());
            String token= JwtUtil.generateToken(claims);
            System.out.println(token);
            return  Result.success(MessageConstant.SUCCESS,token);
        }
            else return Result.error(MessageConstant.LOGIN + MessageConstant.FAILED);
    }
    public Result<UserVO> getUserinfo(){
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        User user = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        System.out.println(userVO.getName());
        return Result.success(userVO);

    }
}