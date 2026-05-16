package com.example.e_shop.service;

import com.example.e_shop.model.DTO.TransactionDTO;
import com.example.e_shop.model.VO.TransactionVO;
import com.example.e_shop.model.entity.Transactionrecords;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.e_shop.result.Result;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-10
 */
public interface TransactionrecordsService extends IService<Transactionrecords> {
    Result addTransactionRecords(TransactionDTO transactionDTO);
    Result <List<TransactionVO>> getTransactionRecords(Long userId, Long otherId);
    Result<List<TransactionVO>> getSellerRecords(Long userId);
    Result<List<TransactionVO>> getBuyerRecords(Long userId);
    Result commitOrders(Long id);
    Result rejectOrders(Long id);
    Result payOrders(Long id);
    Result <TransactionVO> getOrdersDetail(Long id);
}
