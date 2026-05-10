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


-- 消息表
CREATE TABLE messages (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    conversation_id BIGINT NOT NULL,
    sender_user_id BIGINT NOT NULL,      -- 对应 tb_user 表的 id
    receiver_user_id BIGINT NOT NULL,    -- 对应 tb_user 表的 id
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_deleted_sender BOOLEAN DEFAULT FALSE,   -- 发送方是否删除
    is_deleted_receiver BOOLEAN DEFAULT FALSE, -- 接收方是否删除
    reply_to_message_id BIGINT NULL,     -- 引用/回复某条消息
    sent_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 索引
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_user_id ON messages(sender_user_id);
CREATE INDEX idx_messages_receiver_user_id ON messages(receiver_user_id);
CREATE INDEX idx_messages_sent_at ON messages(sent_at);
CREATE INDEX idx_messages_conversation_sent ON messages(conversation_id, sent_at);

-- 外键约束（关联 conversations 表）
ALTER TABLE messages
ADD CONSTRAINT fk_messages_conversation
FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;

-- 外键约束（关联 tb_user 表）
ALTER TABLE messages
ADD CONSTRAINT fk_messages_sender_user
FOREIGN KEY (sender_user_id) REFERENCES tb_user(id);

ALTER TABLE messages
ADD CONSTRAINT fk_messages_receiver_user
FOREIGN KEY (receiver_user_id) REFERENCES tb_user(id);

-- 自引用外键（回复消息功能）
ALTER TABLE messages
ADD CONSTRAINT fk_messages_reply_to
FOREIGN KEY (reply_to_message_id) REFERENCES messages(id) ON DELETE SET NULL;

-- 自动更新 updated_at 的触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_messages_updated_at
BEFORE UPDATE ON messages
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- 为 Supabase Realtime 开启变更监听
ALTER TABLE messages REPLICA IDENTITY FULL;




--Conversation表
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    participant_user_ids BIGINT[] NOT NULL,  -- 两个用户的 ID 数组
    last_message TEXT,
    last_message_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 索引
CREATE INDEX idx_conversations_participants ON conversations USING GIN(participant_user_ids);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at DESC);

-- 触发器
CREATE TRIGGER update_conversations_updated_at
BEFORE UPDATE ON conversations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
--Conversation表
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    participant_user_ids BIGINT[] NOT NULL,  -- 两个用户的 ID 数组
    last_message TEXT,
    last_message_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 索引
CREATE INDEX idx_conversations_participants ON conversations USING GIN(participant_user_ids);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at DESC);

-- 触发器
CREATE TRIGGER update_conversations_updated_at
BEFORE UPDATE ON conversations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
-- transaction_records（交易记录）表
CREATE TABLE transaction_records (
    id BIGSERIAL PRIMARY KEY,                     -- 交易唯一ID（自增）
    seller_id BIGINT NOT NULL,                    -- 卖家ID
    buyer_id BIGINT NOT NULL,                     -- 买家ID
    product_id BIGINT NOT NULL,                   -- 商品ID
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0), -- 交易金额（正数）
    transaction_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(), -- 交易发生时间（带时区）
    transaction_deadline TIMESTAMP WITH TIME ZONE NOT NULL,  -- 交易时间限制（截止时间）

    -- 外键约束（注意：这里不能有分号）
    CONSTRAINT fk_seller FOREIGN KEY (seller_id) REFERENCES tb_user(id),
    CONSTRAINT fk_buyer FOREIGN KEY (buyer_id) REFERENCES tb_user(id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(id),  -- 分号在最后一句才有

    -- 业务约束：交易时间不能晚于截止时间
    CONSTRAINT transaction_time_before_deadline CHECK (transaction_time <= transaction_deadline)
);

-- 常用索引建议（提升查询性能）
CREATE INDEX idx_transaction_seller ON transaction_records(seller_id);
CREATE INDEX idx_transaction_buyer ON transaction_records(buyer_id);
CREATE INDEX idx_transaction_time ON transaction_records(transaction_time);
CREATE INDEX idx_transaction_deadline ON transaction_records(transaction_deadline);  -- 补全表名