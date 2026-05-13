package com.example.e_shop.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductsVO {
    private String name;

    private Long id;

    private Long userId;

    private BigDecimal price;

    private String imageUrl;

    private String description;

    private LocalDateTime createdAt;
    private Boolean isOrder;

    private Boolean show;

    private String typeName;

    private Long detailView;

    private Long typeId;

}
