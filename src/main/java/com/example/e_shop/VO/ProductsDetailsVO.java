package com.example.e_shop.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductsDetailsVO {
    private Long  id;
    private String  name;
    private String  userName;
    private String imageUrl;
    private BigDecimal price;
    private String description;
    private LocalDateTime createdAt;
    private UserVO userVO;
    private Boolean isOrder;
    private Long typeId;
    private Long detailView;
}
