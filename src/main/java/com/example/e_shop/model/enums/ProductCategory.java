package com.example.e_shop.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 商品分类枚举。
 * <p>
 * 对应前端 CONST.CATEGORIES 中的 typeId 值。
 * 枚举名可直接作为展示名使用。
 * </p>
 */
public enum ProductCategory {

    /** 0 - 数码电子 */
    DIGITAL_ELECTRONICS(0L, "数码电子"),
    /** 1 - 生活日用 */
    DAILY_NECESSITIES(1L, "生活日用"),
    /** 2 - 充值代练 */
    RECHARGE_BOOSTING(2L, "充值代练"),
    /** 3 - 其他 */
    OTHER(3L, "其他"),
    /** 4 - 食品酒水 */
    FOOD_DRINKS(4L, "食品酒水"),
    /** 5 - 衣服穿搭 */
    CLOTHING(5L, "衣服穿搭");

    /** 数据库中的 typeId 值 */
    private final Long typeId;

    /** 展示名称 */
    private final String displayName;

    ProductCategory(Long typeId, String displayName) {
        this.typeId = typeId;
        this.displayName = displayName;
    }

    /**
     * 根据 typeId 获取对应的枚举。
     *
     * @param typeId 分类 ID（可为 null）
     * @return 匹配的枚举，null 或无法匹配时返回 null
     */
    public static ProductCategory of(Long typeId) {
        if (typeId == null) return null;
        for (ProductCategory c : values()) {
            if (c.typeId.equals(typeId)) return c;
        }
        return null;
    }

    /**
     * 序列化时只输出 typeId 数值。
     */
    @JsonValue
    public Long getTypeId() {
        return typeId;
    }

    /**
     * 获取中文展示名称。
     */
    public String getDisplayName() {
        return displayName;
    }
}
