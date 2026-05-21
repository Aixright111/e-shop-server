package com.example.e_shop.controller;

import com.example.e_shop.model.DTO.UserDTO;
import com.example.e_shop.model.VO.UserVO;
import com.example.e_shop.model.entity.User;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.UserService;
import com.example.e_shop.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


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
    @Autowired
    private VerifyCodeService verifyCodeService;
    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return Result.error("邮箱地址不能为空");
        }
        verifyCodeService.sendCode(email);
        return Result.success();
    }

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

    @PostMapping("/reset-password")
    public Result resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String password = body.get("password");
        if (email == null || email.isBlank()) {
            return Result.error("邮箱地址不能为空");
        }
        if (code == null || code.isBlank()) {
            return Result.error("验证码不能为空");
        }
        if (password == null || password.isBlank()) {
            return Result.error("密码不能为空");
        }
        return userService.resetPassword(email, code, password);
    }
}
