package com.example.e_shop.DTO;

import lombok.Data;

@Data
public class GetProductsDTO {
 private Integer PageSize;
 private Integer pageNum;
 private Long userId;
}
