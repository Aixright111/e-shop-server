-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE public.tb_user (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  user_image VARCHAR(255)
);

-- ============================================
-- 2. 商品表
-- ============================================
CREATE TABLE public.products (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  price NUMERIC NOT NULL CHECK (price >= 0),
  image_url TEXT,
  description TEXT,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  user_id BIGINT REFERENCES public.tb_user(id),
  show BOOLEAN DEFAULT TRUE,
  typeid BIGINT DEFAULT 3,
  typename TEXT,
  detailviews BIGINT DEFAULT 0,
  ordercount BIGINT DEFAULT 0,
  isorder BOOLEAN DEFAULT FALSE,
  banner_urls TEXT[] DEFAULT '{}',
  -- embedding 类型：如果安装了 vector 扩展用 vector，否则用 TEXT
  embedding VECTOR(1536),  -- 或者用 TEXT
  beembedding BOOLEAN DEFAULT FALSE
);

-- ============================================
-- 3. 会话表
-- ============================================
CREATE TABLE public.conversations (
  id BIGSERIAL PRIMARY KEY,
  participant_user_ids BIGINT[] NOT NULL,  -- 修复：指定数组元素类型
  last_message TEXT,
  last_message_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 4. 消息表
-- ============================================
CREATE TABLE public.messages (
  id BIGSERIAL PRIMARY KEY,
  conversation_id BIGINT NOT NULL REFERENCES public.conversations(id),
  sender_user_id BIGINT NOT NULL REFERENCES public.tb_user(id),
  receiver_user_id BIGINT NOT NULL REFERENCES public.tb_user(id),
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  is_deleted_sender BOOLEAN DEFAULT FALSE,
  is_deleted_receiver BOOLEAN DEFAULT FALSE,
  reply_to_message_id BIGINT REFERENCES public.messages(id),
  sent_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 5. 收藏夹表
-- ============================================
CREATE TABLE public.favorites (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES public.tb_user(id) ON DELETE CASCADE,
  product_id BIGINT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  typeid BIGINT DEFAULT 3,
  UNIQUE(user_id, product_id)  -- 防止重复收藏
);

-- ============================================
-- 6. 交易记录表
-- ============================================
CREATE TABLE public.transactionrecords (
  id BIGSERIAL PRIMARY KEY,
  sellerid BIGINT NOT NULL REFERENCES public.tb_user(id),
  buyerid BIGINT NOT NULL REFERENCES public.tb_user(id),
  productid BIGINT NOT NULL REFERENCES public.products(id),
  amount NUMERIC NOT NULL CHECK (amount > 0),
  transactiontime TIMESTAMP WITHOUT TIME ZONE,
  transactiondeadline TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  is_commit BOOLEAN DEFAULT FALSE,
  is_pay BOOLEAN DEFAULT FALSE,
  is_expired BOOLEAN DEFAULT FALSE,
  is_reject BOOLEAN DEFAULT FALSE
);