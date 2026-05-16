package com.example.e_shop.service;

import com.example.e_shop.model.entity.Conversations;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConversationsService extends IService<Conversations> {

    List<Conversations> getConversationsByUserId(Long userId);
}
