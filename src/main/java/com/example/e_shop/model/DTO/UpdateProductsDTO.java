package com.example.e_shop.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductsDTO {
    private Long id;
    private String name ;
    private BigDecimal price ;
    private String imageUrl ;
    private String description;
    private Long typeId;
    private List<String> bannerUrls;
}
