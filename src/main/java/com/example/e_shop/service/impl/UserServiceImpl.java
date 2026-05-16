package com.example.e_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.e_shop.model.DTO.UserDTO;
import com.example.e_shop.model.VO.UserVO;
import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.constant.MessageConstant;
import com.example.e_shop.model.entity.User;
import com.example.e_shop.mapper.UserMapper;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.e_shop.util.JwtUtil;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-07
 */

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private  StringRedisTemplate stringRedisTemplate;
    public Result register(UserDTO userDTO) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        String passwordMD5 = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());
        user.setPassword(passwordMD5);
        user.setName(userDTO.getUsername());
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
            ThreadLocalUtil.set(claims);
            String token= JwtUtil.generateToken(claims);
            stringRedisTemplate.opsForValue().set(user.getName(),token);
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
        userVO.setAvatarUrl(user.getUserImage());
        return Result.success(userVO);
    }
    public Result updateUserinfo(UserDTO userDTO){
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        System.out.println(userId);
        User user = userMapper.selectById(userId);
        user.setId(userId);
        user.setName(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setUserImage(userDTO.getAvatarUrl());
        System.out.println(userDTO.getAvatarUrl());
        user.setUpdatedAt(LocalDateTime.now());
        if(userMapper.updateById(user)==0){
            return Result.error(MessageConstant.FAILED);
        }
        else return Result.success(MessageConstant.SUCCESS);
    }
    public Result<UserVO> getUserInfoById(Long userId){
        UserVO userVO=new UserVO();
        User user=userMapper.selectById(userId);
        if (user!=null)System.out.println("没获取userbyid");
        BeanUtils.copyProperties(user,userVO);
        userVO.setAvatarUrl(user.getUserImage());
        if(userVO!=null)
        return Result.success(MessageConstant.SUCCESS,userVO);
        else return Result.error(MessageConstant.FAILED);
    }

}