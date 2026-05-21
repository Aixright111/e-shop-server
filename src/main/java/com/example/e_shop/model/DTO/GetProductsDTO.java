package com.example.e_shop.model.DTO;

import lombok.Data;

@Data
public class GetProductsDTO {
    private Integer pageSize;
    private Integer PageNum;
    private Long userId;
    private String typeName;
    private Long typeId;
    private String name;
    private String sortField;  // 排序字段：price, createTime 等
    private String embedding;
    private String sortOrder;  // 排序方式：ASC, DESC
}
