package com.example.e_shop.util;

import com.example.e_shop.model.DTO.GetProductsDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheKeyUtil {

    public static String getProductsKey(GetProductsDTO dto) {
        // 防御：如果 dto 为 null，返回默认 key
        if (dto == null) {
            return "products:default";
        }

        // 处理 null 值，设置默认值
        int pageNum = dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 10;
        Long typeId = dto.getTypeId() != null ? dto.getTypeId() : -1;
        Long userId = dto.getUserId() != null ? dto.getUserId() : 0;
        String name = dto.getName() !=null ? dto.getName() : "null";
        // 生成唯一的缓存 Key
        return String.format("products:%d:%d:%d:%d:%s", pageNum, pageSize, typeId, userId, name);
    }
}