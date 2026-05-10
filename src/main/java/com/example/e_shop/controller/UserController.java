package com.example.e_shop.controller;

import com.example.e_shop.DTO.UserDTO;
import com.example.e_shop.VO.UserVO;
import com.example.e_shop.entity.User;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author e-shop
 * @since 2026-05-07
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public Result<User> register (@RequestBody UserDTO userDTO){
        System.out.println(userDTO.getEmail());
        return userService.register(userDTO);
    }
    @PostMapping("/login")
    public Result<UserDTO> login (@RequestBody UserDTO userDTO){
        return userService.login(userDTO);
    }
    @GetMapping("/info")
    public Result<UserVO> getInfo(){
        return userService.getUserinfo();
    }
    @PutMapping("/update")
    public  Result updateUserInfo(@RequestBody UserDTO userDTO){
        return userService.updateUserinfo(userDTO);
    }
    @GetMapping("/getInfoById/{userId}")
    public  Result<UserVO> getInfoById( @PathVariable Long userId){
        System.out.println("getInfoById");
        return  userService.getUserInfoById(userId);
    }
}
