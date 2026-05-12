package com.example.e_shop.controller;

import com.example.e_shop.DTO.TransactionDTO;
import com.example.e_shop.VO.TransactionVO;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.TransactionrecordsService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author e-shop
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/orders")
public class TransactionrecordsController {
   @Autowired
    TransactionrecordsService transactionrecordsService;
   @PostMapping("/offer")
   public Result addTransactionRecords(@RequestBody TransactionDTO transactionDTO){
       System.out.println(transactionDTO.getHours());
       return  transactionrecordsService.addTransactionRecords(transactionDTO);
   }
   @GetMapping("/get/{userId}/{otherUserId}")
    public Result<List<TransactionVO>> getTransactionRecords(@PathVariable("userId") Long userId, @PathVariable("otherUserId")  Long otherUserId){
       return  transactionrecordsService.getTransactionRecords(userId,otherUserId);
   }
   @GetMapping("received/{userId}")
   public Result<List<TransactionVO>> getReceivedRecords(@PathVariable("userId")Long userId){
       return transactionrecordsService.getSellerRecords(userId);
   }

   @GetMapping("sent/{userId}")
   public Result<List<TransactionVO>>getSentRecords(@PathVariable("userId")Long userId){
       return transactionrecordsService.getBuyerRecords(userId);
   }
   @GetMapping("detail/{orderId}")
    public Result<TransactionVO> getOrderDetail(@PathVariable("orderId") Long orderId){
       return  transactionrecordsService.getOrdersDetail(orderId);
   }
   @PutMapping("pay/{orderId}")
   public  Result payOrder(@PathVariable("orderId") Long orderId){
       return  transactionrecordsService.payOrders(orderId);
   }
   @PutMapping("commit/{orderId}")
   public  Result commitOrder(@PathVariable("orderId") Long orderId){
       return  transactionrecordsService.commitOrders(orderId);
}
    @PutMapping("reject/{orderId}")
    public  Result rejectOrder(@PathVariable("orderId") Long orderId){
        return  transactionrecordsService.rejectOrders(orderId);
    }
}
