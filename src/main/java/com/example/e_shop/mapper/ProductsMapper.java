package com.example.e_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.e_shop.model.entity.Products;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author e-shop
 * @since 2026-05-08
 */
public interface ProductsMapper extends BaseMapper<Products> {
    void incrementDetailView(Products products);

    /** 用 pgvector <=> 查询同类中向量最相似的 N 个商品名称 */
    @Select("<script>"
            + "SELECT name FROM products "
            + "WHERE typeid = #{typeId} AND embedding IS NOT NULL "
            + "<if test='excludedIds != null and !excludedIds.isEmpty()'>"
            + "AND id NOT IN "
            + "<foreach collection='excludedIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "</if>"
            + "ORDER BY embedding &lt;=&gt; #{vector}::vector "
            + "LIMIT #{limit}"
            + "</script>")
    List<String> findTopSimilarNames(@Param("vector") String vector, @Param("typeId") Long typeId,
                                     @Param("excludedIds") List<Long> excludedIds, @Param("limit") int limit);
}
