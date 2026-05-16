package com.example.e_shop.model.DTO;

import lombok.Data;

@Data
public class GetProductsDTO {
 private Integer PageSize;
 private Integer pageNum;
 private Long userId;
 private String typeName;
 private Long typeId;
 private String name;
}
