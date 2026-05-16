package com.example.e_shop.model.VO;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionVO {
    private Long id;

    private Long sellerid;

    private Long buyerid;

    private Long productId;

    private UserVO userVO;

    private Long price;

    private String name;

    private BigDecimal amount;

    private Boolean isCommit;
    private Boolean isReject;
    private Boolean isPay;

    private LocalDateTime transactiontime;

    private  Long hours;

    private String imageUrl;
    private Boolean isExpired;
    private LocalDateTime transactiondeadline;
}
