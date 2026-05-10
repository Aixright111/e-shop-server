package com.example.e_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.e_shop.entity.Transactionrecords;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author e-shop
 * @since 2026-05-10
 */
public interface TransactionrecordsMapper extends BaseMapper<Transactionrecords> {
    @Select("SELECT * FROM transactionrecords " +
            "WHERE (sellerid = #{currentUserId} AND buyerid = #{otherUserId}) " +
            "   OR (sellerid = #{otherUserId} AND buyerid = #{currentUserId}) " +
            "ORDER BY transactiontime DESC")
    List<Transactionrecords> getTransactionsBetweenUsers(@Param("currentUserId") Long currentUserId,
                                                         @Param("otherUserId") Long otherUserId);




}
