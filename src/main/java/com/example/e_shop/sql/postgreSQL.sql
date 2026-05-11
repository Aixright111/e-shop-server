-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.conversations (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  participant_user_ids ARRAY NOT NULL,
  last_message text,
  last_message_at timestamp without time zone DEFAULT now(),
  created_at timestamp without time zone DEFAULT now(),
  updated_at timestamp without time zone DEFAULT now(),
  CONSTRAINT conversations_pkey PRIMARY KEY (id)
);
CREATE TABLE public.messages (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  conversation_id bigint NOT NULL,
  sender_user_id bigint NOT NULL,
  receiver_user_id bigint NOT NULL,
  content text NOT NULL,
  is_read boolean DEFAULT false,
  is_deleted_sender boolean DEFAULT false,
  is_deleted_receiver boolean DEFAULT false,
  reply_to_message_id bigint,
  sent_at timestamp without time zone DEFAULT now(),
  updated_at timestamp without time zone DEFAULT now(),
  CONSTRAINT messages_pkey PRIMARY KEY (id),
  CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES public.conversations(id),
  CONSTRAINT fk_messages_sender_user FOREIGN KEY (sender_user_id) REFERENCES public.tb_user(id),
  CONSTRAINT fk_messages_receiver_user FOREIGN KEY (receiver_user_id) REFERENCES public.tb_user(id),
  CONSTRAINT fk_messages_reply_to FOREIGN KEY (reply_to_message_id) REFERENCES public.messages(id)
);
CREATE TABLE public.products (
  id bigint NOT NULL DEFAULT nextval('products_id_seq'::regclass),
  name text NOT NULL,
  price numeric NOT NULL CHECK (price >= 0::numeric),
  image_url text,
  description text,
  created_at timestamp without time zone DEFAULT now(),
  updated_at timestamp without time zone DEFAULT now(),
  user_id bigint,
  CONSTRAINT products_pkey PRIMARY KEY (id),
  CONSTRAINT fk_products_tb_user FOREIGN KEY (user_id) REFERENCES public.tb_user(id)
);
CREATE TABLE public.tb_user (
  id bigint NOT NULL DEFAULT nextval('tb_user_id_seq'::regclass),
  name character varying NOT NULL,
  email character varying NOT NULL UNIQUE,
  password character varying NOT NULL,
  created_at timestamp without time zone DEFAULT now(),
  updated_at timestamp without time zone DEFAULT now(),
  user_image character varying,
  CONSTRAINT tb_user_pkey PRIMARY KEY (id)
);
CREATE TABLE public.transactionrecords (
  id bigint NOT NULL DEFAULT nextval('transaction_records_id_seq'::regclass),
  sellerid bigint NOT NULL,
  buyerid bigint NOT NULL,
  productid bigint NOT NULL,
  amount numeric NOT NULL CHECK (amount > 0::numeric),
  transactiontime timestamp without time zone,
  transactiondeadline timestamp without time zone NOT NULL,
  is_commit boolean DEFAULT false,
  is_pay boolean DEFAULT false,
  CONSTRAINT transactionrecords_pkey PRIMARY KEY (id),
  CONSTRAINT fk_seller FOREIGN KEY (sellerid) REFERENCES public.tb_user(id),
  CONSTRAINT fk_buyer FOREIGN KEY (buyerid) REFERENCES public.tb_user(id),
  CONSTRAINT fk_product FOREIGN KEY (productid) REFERENCES public.products(id)
);