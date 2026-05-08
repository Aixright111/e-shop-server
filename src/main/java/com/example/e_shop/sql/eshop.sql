-- 创建商品表
CREATE TABLE tb_products (
    -- 商品ID：使用 bigint 自增主键
    id BIGSERIAL PRIMARY KEY,

    -- 商品名字：不允许为空
    name TEXT NOT NULL,

    -- 商品价格：使用 numeric 类型精确存储金额 (整数部分最多10位，小数2位)
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),

    -- 图片URL：存储地址字符串
    image_url TEXT,

    -- 详细介绍：使用 TEXT 类型长度不限
    description TEXT,

    -- 可选：添加时间戳便于管理（推荐加上）
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 可选：添加一个更新 updated_at 的触发器函数（如果需要在修改时自动更新时间）
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_tb_products_updated_at
    BEFORE UPDATE ON tb_products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();