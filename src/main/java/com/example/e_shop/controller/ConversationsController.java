package com.example.e_shop.controller;

import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.entity.Conversations;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.ConversationsService;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversations")
public class ConversationsController {

    @Autowired
    private ConversationsService conversationsService;


}
