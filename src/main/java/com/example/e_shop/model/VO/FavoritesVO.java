package com.example.e_shop.model.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoritesVO {
    private Long id;
    private Long productId;
    private Long typeId;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;
}
