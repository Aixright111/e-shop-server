package com.example.e_shop.service;

import com.example.e_shop.DTO.UserDTO;
import com.example.e_shop.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.e_shop.result.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-07
 */
public interface UserService extends IService<User> {
     Result register(UserDTO userDTO);
     Result login(UserDTO userDTO);
     Result getUserinfo();
     Result updateUserinfo(UserDTO userDTO);
}
