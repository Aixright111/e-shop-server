package com.example.e_shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2026-05-08
 */
@Getter
@Setter
@ToString
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private BigDecimal price;

    private String imageUrl;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
