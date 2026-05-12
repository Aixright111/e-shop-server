package com.example.e_shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author e-shop
 * @since 2026-05-10
 */
@Getter
@Setter
@ToString

public class Transactionrecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long sellerid;

    private Long buyerid;

    private Long productid;

    private BigDecimal amount;

    private LocalDateTime transactiontime;

    private LocalDateTime transactiondeadline;
    @TableField("is_commit")
    private Boolean isCommit;
    @TableField("is_pay")
    private Boolean isPay;
    @TableField("is_expired")
    private Boolean isExpired;
    @TableField("is_reject")
    private Boolean isReject;
}
