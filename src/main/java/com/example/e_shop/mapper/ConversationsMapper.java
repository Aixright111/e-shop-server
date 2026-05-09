package com.example.e_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.e_shop.entity.Conversations;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationsMapper extends BaseMapper<Conversations> {

    // 查询两个用户之间的会话
    @Select("SELECT * FROM conversations " +
            "WHERE participant_user_ids @> ARRAY[#{userId1}, #{userId2}]::BIGINT[] " +
            "AND array_length(participant_user_ids, 1) = 2 " +
            "LIMIT 1")
    Conversations findByParticipants(@Param("userId1") Long userId1,
                                    @Param("userId2") Long userId2);

    // 查询当前用户的所有会话，按最后消息时间倒序
    @Select("SELECT * FROM conversations " +
            "WHERE #{userId} = ANY(participant_user_ids) " +
            "ORDER BY updated_at DESC")
    List<Conversations> findByUserId(@Param("userId") Long userId);
}
