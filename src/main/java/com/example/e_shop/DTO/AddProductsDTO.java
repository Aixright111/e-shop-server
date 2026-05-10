package com.example.e_shop.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductsDTO {
    private String name ;
    private BigDecimal price ;
    private String imageUrl ;
    private String description;
}
