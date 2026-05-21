package com.example.e_shop.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author e-shop
 * @since 2026-05-08
 */
@Getter
@Setter
@ToString
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private BigDecimal price;

    private String imageUrl;

    private String description;

    private LocalDateTime createdAt;


    @TableField("isorder")
    private Boolean isOrder;

    private LocalDateTime updatedAt;

    private Boolean show;
    @TableField("typename")
    private String typeName;
    @TableField("typeid")
    private Long typeId;
    @TableField(value = "banner_urls", typeHandler = com.example.e_shop.handler.StringListTypeHandler.class)
    private List<String> bannerUrls;
    private Long userId;

    @TableField("detailviews")

    private Long detailView;

    /** 商品向量嵌入（pgvector，1024 维），用于语义搜索 */
    @TableField(value = "embedding", typeHandler = com.example.e_shop.handler.Vector1024TypeHandler.class)
    private float[] embedding;

    @TableField(value = "beembedding")
    private  boolean beEmbedding;
}
