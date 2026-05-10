package com.example.e_shop.service.impl;

import com.example.e_shop.DTO.TransactionDTO;
import com.example.e_shop.VO.TransactionVO;
import com.example.e_shop.constant.MessageConstant;
import com.example.e_shop.entity.Products;
import com.example.e_shop.entity.Transactionrecords;
import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.mapper.TransactionrecordsMapper;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.TransactionrecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-10
 */
@Service
public class TransactionrecordsServiceImpl extends ServiceImpl<TransactionrecordsMapper, Transactionrecords> implements TransactionrecordsService {
    @Autowired
    TransactionrecordsMapper transactionrecordsMapper;
    @Autowired
    ProductsMapper productsMapper;

    public Result addTransactionRecords(TransactionDTO transactionDTO){
                 Transactionrecords transactionrecords=new Transactionrecords();
                 transactionrecords.setProductid(transactionDTO.getProductId());
                 transactionrecords.setBuyerid(transactionDTO.getBuyerId());
                 transactionrecords.setSellerid(transactionDTO.getSellerId());
                 transactionrecords.setAmount(transactionDTO.getAmount());
                 transactionrecords.setTransactiontime(LocalDateTime.now());
                 transactionrecords.setTransactiondeadline(LocalDateTime.now().plusHours(transactionDTO.getHours()));
                 if(transactionrecordsMapper.insert(transactionrecords)==0)
                 {
                     return Result.error();
                 }
                 return Result.success();
             }

    public Result<List<TransactionVO>> getTransactionRecords(Long userId,Long otherId){

        List<Transactionrecords> transactionrecordsList=  transactionrecordsMapper.getTransactionsBetweenUsers(userId,otherId);
        List<TransactionVO> transactionVOList= transactionrecordsList.stream().map(
                transactionrecords ->{
                    Long productsId=transactionrecords.getProductid();
                    Products products=productsMapper.selectById(productsId);
                    TransactionVO transactionVO=new TransactionVO();
                    BeanUtils.copyProperties(transactionrecords,transactionVO);
                    transactionVO.setName(products.getName());

                    Duration duration = Duration.between(transactionrecords.getTransactiontime(), transactionrecords.getTransactiondeadline());
                    long hours = duration.toHours();
                    transactionVO.setImageUrl(products.getImageUrl());
                    transactionVO.setHours(hours);
                    return transactionVO;
        } ).toList();
        return Result.success(MessageConstant.SUCCESS,transactionVOList);
    }
}
