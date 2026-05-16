package com.example.e_shop.controller;

import com.example.e_shop.service.ConversationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversations")
public class ConversationsController {

    @Autowired
    private ConversationsService conversationsService;


}
