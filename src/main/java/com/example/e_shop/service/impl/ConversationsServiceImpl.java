package com.example.e_shop.service.impl;

import com.example.e_shop.entity.Conversations;
import com.example.e_shop.mapper.ConversationsMapper;
import com.example.e_shop.service.ConversationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationsServiceImpl extends ServiceImpl<ConversationsMapper, Conversations> implements ConversationsService {

    @Override
    public List<Conversations> getConversationsByUserId(Long userId) {
        return baseMapper.findByUserId(userId);
    }
}
