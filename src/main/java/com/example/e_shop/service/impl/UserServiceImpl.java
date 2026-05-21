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
        log.debug("收到注册请求: email={}, username={}", userDTO.getEmail(), userDTO.getUsername());

        // 校验邮箱验证码
        String redisKey = "verify_code:" + userDTO.getEmail();
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);
        log.debug("验证码校验: email={}, 输入code={}, Redis中code={}, key={}",
                userDTO.getEmail(), userDTO.getCode(), cachedCode, redisKey);
        if (cachedCode == null) {
            log.warn("验证码已过期: email={}", userDTO.getEmail());
            return Result.error("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(userDTO.getCode())) {
            log.warn("验证码错误: email={}, 输入={}, 正确={}", userDTO.getEmail(), userDTO.getCode(), cachedCode);
            return Result.error("验证码错误");
        }
        // 验证通过，删除已使用的验证码
        stringRedisTemplate.delete(redisKey);
        log.debug("验证码校验通过，已删除Redis中的验证码: key={}", redisKey);

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
    public Result resetPassword(String email, String code, String password) {
        log.debug("收到密码重置请求: email={}", email);

        // 校验验证码
        String redisKey = "verify_code:" + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);
        log.debug("验证码校验: email={}, 输入code={}, Redis中code={}", email, code, cachedCode);
        if (cachedCode == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            return Result.error("验证码错误");
        }

        // 验证通过，删除已使用的验证码
        stringRedisTemplate.delete(redisKey);

        // 通过邮箱查找用户并更新密码
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        if (user == null) {
            log.warn("用户不存在: email={}", email);
            return Result.error("该邮箱未注册");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setUpdatedAt(LocalDateTime.now());
        if (userMapper.updateById(user) == 0) {
            log.error("密码更新失败: email={}", email);
            return Result.error("密码重置失败");
        }
        log.info("密码重置成功: email={}", email);
        return Result.success("密码重置成功");
    }

    public Result<UserVO> getUserInfoById(Long userId){
        UserVO userVO=new UserVO();
        User user=userMapper.selectById(userId);
        BeanUtils.copyProperties(user,userVO);
        userVO.setAvatarUrl(user.getUserImage());
        if(userVO!=null)
        return Result.success(MessageConstant.SUCCESS,userVO);
        else return Result.error(MessageConstant.FAILED);
    }

}