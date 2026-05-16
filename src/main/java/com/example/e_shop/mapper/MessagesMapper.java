package com.example.e_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.e_shop.model.entity.Messages;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MessagesMapper extends BaseMapper<Messages> {

    // 查询会话中的所有消息（过滤已删除）
    @Select("SELECT m.*, u.name as sender_name " +
            "FROM messages m " +
            "LEFT JOIN tb_user u ON m.sender_user_id = u.id " +
            "WHERE m.conversation_id = #{conversationId} " +
            "AND NOT (m.sender_user_id = #{currentUserId} AND m.is_deleted_sender = true) " +
            "AND NOT (m.receiver_user_id = #{currentUserId} AND m.is_deleted_receiver = true) " +
            "ORDER BY m.sent_at ASC")
    List<Messages> getConversationMessages(@Param("conversationId") Long conversationId,
                                          @Param("currentUserId") Long currentUserId);
}