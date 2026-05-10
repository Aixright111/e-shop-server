package com.example.e_shop.DTO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class TransactionDTO {
    private static final long serialVersionUID = 1L;


    private Long id;

    private Long sellerId;

    private Long buyerId;

    private Long productId;

    private Long hours;

    private BigDecimal amount;
}
