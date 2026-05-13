package com.example.e_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.e_shop.DTO.TransactionDTO;
import com.example.e_shop.Lock.RedisDistributedLock;
import com.example.e_shop.VO.TransactionVO;
import com.example.e_shop.VO.UserVO;
import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.constant.MessageConstant;
import com.example.e_shop.entity.Products;
import com.example.e_shop.entity.Transactionrecords;
import com.example.e_shop.entity.User;
import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.mapper.TransactionrecordsMapper;
import com.example.e_shop.mapper.UserMapper;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.TransactionrecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-10
 */
@Slf4j
@Service
public class TransactionrecordsServiceImpl extends ServiceImpl<TransactionrecordsMapper, Transactionrecords> implements TransactionrecordsService {
    @Autowired
    TransactionrecordsMapper transactionrecordsMapper;
    @Autowired
    ProductsMapper productsMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    private RedisDistributedLock lock; //redis实现分布式锁
    public Result addTransactionRecords(TransactionDTO transactionDTO){
                 Transactionrecords transactionrecords=new Transactionrecords();
                 transactionrecords.setProductid(transactionDTO.getProductId());
                 transactionrecords.setBuyerid(transactionDTO.getBuyerId());
                 transactionrecords.setSellerid(transactionDTO.getSellerId());
                 transactionrecords.setAmount(transactionDTO.getAmount());
                 transactionrecords.setTransactiontime(LocalDateTime.now());
                 String locKey="locKey:"+transactionDTO.getProductId();
                 String lockId= UUID.randomUUID().toString();
                 try{
                     if(lock.tryLock(locKey,lockId,10000)){
                         transactionrecords.setTransactiondeadline(LocalDateTime.now().plusHours(transactionDTO.getHours()));
                         transactionrecordsMapper.insert(transactionrecords);
                         Thread.sleep(3000);
                          return Result.success();
                     }
                     else {
                         log.info("已被报价");
                         throw new RuntimeException("获取锁失败");
                     }

                 }
                 catch (Exception e){}
                 finally {
                         lock.releaseLock(locKey,lockId);
                 }

                 return Result.error();
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
    public Result<List<TransactionVO>> getSellerRecords(Long userId){
        QueryWrapper queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("sellerid",userId);
        List<Transactionrecords> transactionrecordsList=  transactionrecordsMapper.selectList(queryWrapper);
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
        return Result.success(transactionVOList);
    }
    public Result<List<TransactionVO>> getBuyerRecords(Long userId){
        QueryWrapper queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("buyerid",userId);
        List<Transactionrecords> transactionrecordsList=  transactionrecordsMapper.selectList(queryWrapper);
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
        return Result.success(transactionVOList);
    }
    public Result commitOrders(Long id){
        Transactionrecords transactionrecords=transactionrecordsMapper.selectById(id);
        transactionrecords.setIsCommit(true);
        Products products=productsMapper.selectById(transactionrecords.getProductid());
        products.setShow(false);
        productsMapper.updateById(products);
        transactionrecordsMapper.updateById(transactionrecords);
        return Result.success(MessageConstant.SUCCESS);
    }
    public Result rejectOrders(Long id){
        Transactionrecords transactionrecords=transactionrecordsMapper.selectById(id);
        transactionrecords.setIsReject(true);
        Products products=productsMapper.selectById(transactionrecords.getProductid());
        products.setShow(true);
        productsMapper.updateById(products);
        transactionrecordsMapper.updateById(transactionrecords);
        return Result.success(MessageConstant.SUCCESS);
    }
    public Result payOrders(Long id){
        Transactionrecords transactionrecords=transactionrecordsMapper.selectById(id);
        transactionrecords.setIsPay(true);
        transactionrecords.setTransactiondeadline( LocalDateTime.now().plusHours(720));
        transactionrecordsMapper.updateById(transactionrecords);
        return Result.success(MessageConstant.SUCCESS);
    }
    public Result<TransactionVO> getOrdersDetail(Long id){
        Transactionrecords transactionrecords=transactionrecordsMapper.selectById(id);
        Long productsId=transactionrecords.getProductid();
        Products products=productsMapper.selectById(productsId);
        TransactionVO transactionVO=new TransactionVO();
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        User user=new User();
        if(userId==transactionrecords.getSellerid()){
           user = userMapper.selectById(transactionrecords.getBuyerid());
        }
        else{ user = userMapper.selectById(transactionrecords.getSellerid());}
        UserVO userVO =new UserVO();
        BeanUtils.copyProperties(user,userVO);
        userVO.setAvatarUrl(user.getUserImage());
        BeanUtils.copyProperties(transactionrecords,transactionVO);
        transactionVO.setUserVO(userVO);
        transactionVO.setName(products.getName());
        Duration duration = Duration.between(transactionrecords.getTransactiontime(), transactionrecords.getTransactiondeadline());
        long hours = duration.toHours();
        transactionVO.setImageUrl(products.getImageUrl());
        transactionVO.setHours(hours);
        return Result.success(transactionVO);
    }
}
