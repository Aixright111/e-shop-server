# e-shop API 文档

Base URL: `http://localhost:8080`

认证方式：请求头 `Authorization: Bearer <token>`

统一响应格式：
```json
{
  "code": 1,       // 1=成功, 0=失败
  "msg": "success",
  "data": {}
}
```

---

## 1. 用户模块 `/user`

### 1.1 注册
**POST** `/user/register`

Body:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "code": "string"
}
```
Response: `Result`

### 1.2 登录
**POST** `/user/login`

Body:
```json
{
  "username": "string",
  "password": "string"
}
```
Response: `Result` → data 为 token 字符串

### 1.3 发送验证码
**POST** `/user/send/code`

Body:
```json
{
  "email": "string"
}
```
Response: `Result`

### 1.4 重置密码
**POST** `/user/reset-password`

Body:
```json
{
  "email": "string",
  "code": "string",
  "password": "string"
}
```
Response: `Result`

### 1.5 获取用户信息
**GET** `/user/info`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "string",
    "password": "string",
    "phone": "string",
    "email": "string",
    "avatarUrl": "string",
    "deleted": 0,
    "role": "ROLE_USER",
    "createTime": "2026-05-21T12:00:00"
  }
}
```

### 1.6 更新用户信息
**PUT** `/user/update`

Body:
```json
{
  "id": 1,
  "phone": "string",
  "email": "string",
  "avatarUrl": "string",
  "username": "string"
}
```
Response: `Result`

---

## 2. 商品模块 `/products`

### 2.1 发布商品
**POST** `/products/add`

Body:
```json
{
  "name": "string",
  "description": "string",
  "price": 0.0,
  "imageUrl": "string",
  "typeId": 1,
  "userId": 1
}
```
Response: `Result`

### 2.2 商品列表（分页）
**POST** `/products/list`

Body:
```json
{
  "page": 1,
  "pageSize": 10,
  "name": "string",
  "typeId": 1,
  "userId": 1,
  "minPrice": 0.0,
  "maxPrice": 999.0
}
```
Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "string",
        "price": 0.0,
        "imageUrl": "string",
        "description": "string",
        "typeId": 1,
        "status": 1,
        "detailView": 10
      }
    ],
    "total": 100,
    "pages": 10
  }
}
```

### 2.3 商品详情
**GET** `/products/details/{id}`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "string",
    "description": "string",
    "price": 0.0,
    "imageUrl": "string",
    "typeId": 1,
    "status": 1,
    "detailView": 10,
    "userId": 1
  }
}
```

### 2.4 删除商品
**DELETE** `/products/delete/{productsId}`

Response: `Result`

### 2.5 更新商品
**PUT** `/products/update`

Body:
```json
{
  "id": 1,
  "name": "string",
  "description": "string",
  "price": 0.0,
  "imageUrl": "string",
  "typeId": 1,
  "status": 1
}
```
Response: `Result`

### 2.6 AI 智能搜索
**POST** `/products/aiList`

Body: 同 2.2 商品列表参数

Response: 同 2.2

---

## 3. 收藏模块 `/favorites`

### 3.1 添加收藏
**POST** `/favorites/add/{productId}`

Response: `Result`

### 3.2 收藏列表
**GET** `/favorites/list`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "typeId": 1,
      "name": "string",
      "price": 0.0,
      "imageUrl": "string",
      "description": "string",
      "createdAt": "2026-05-21T12:00:00"
    }
  ]
}
```

### 3.3 取消收藏
**DELETE** `/favorites/{productId}`

Response: `Result`

### 3.4 AI 智能推荐
**GET** `/favorites/recommend`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": ["商品A", "商品B", "商品C"]
}
```

---

## 4. 聊天模块

### 4.1 发送消息
**POST** `/chat/send`

Body:
```json
{
  "conversationId": 1,
  "receiverId": 2,
  "content": "string"
}
```
Response: `Result`

### 4.2 获取会话消息
**GET** `/chat/messages/{conversationId}?page=1&pageSize=20`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "conversationId": 1,
        "senderId": 1,
        "receiverId": 2,
        "content": "string",
        "sentAt": "2026-05-21T12:00:00",
        "isRead": false
      }
    ],
    "total": 50,
    "pages": 3
  }
}
```

### 4.3 标记已读
**PUT** `/chat/read/{conversationId}`

Response: `Result`

### 4.4 获取会话列表
**GET** `/conversations/list`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "conversationId": 1,
      "otherUserId": 2,
      "otherUsername": "string",
      "otherAvatarUrl": "string",
      "lastMessage": "string",
      "lastMessageTime": "2026-05-21T12:00:00",
      "unreadCount": 3
    }
  ]
}
```

### 4.5 未读消息总数
**GET** `/messages/unread/total`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": 5
}
```

---

## 5. 订单模块 `/transaction`

### 5.1 创建订单
**POST** `/transaction/create`

Body:
```json
{
  "productId": 1,
  "buyerId": 1,
  "sellerId": 2
}
```
Response: `Result`

### 5.2 买家订单列表
**POST** `/transaction/buyer`

Body:
```json
{
  "page": 1,
  "pageSize": 10,
  "status": 1  // 可选
}
```
Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "productId": 1,
        "productName": "string",
        "productImage": "string",
        "buyerId": 1,
        "sellerId": 2,
        "sellerName": "string",
        "price": 100.0,
        "status": 1,
        "statusName": "待付款",
        "createdAt": "2026-05-21T12:00:00"
      }
    ],
    "total": 20,
    "pages": 2
  }
}
```

状态码说明：1=待付款 2=待发货 3=待收货 4=已完成 5=已拒绝 6=已取消

### 5.3 卖家订单列表
**POST** `/transaction/seller`

参数同 5.2

### 5.4 支付订单
**PUT** `/transaction/pay/{id}`

Response: `Result`

### 5.5 拒绝订单
**PUT** `/transaction/reject/{id}`

Response: `Result`

### 5.6 确认收货
**PUT** `/transaction/commit/{id}`

Response: `Result`

---

## 6. 轮播图模块 `/banner`

### 6.1 获取轮播图列表
**GET** `/banner/list`

Response:
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "imageUrl": "string",
      "productId": 1,
      "sort": 1
    }
  ]
}
```

---

## 数据模型

### User 用户
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| username | String | 用户名 |
| password | String | MD5 加密密码 |
| phone | String | 手机号 |
| email | String | 邮箱 |
| avatarUrl | String | 头像 |
| deleted | Integer | 逻辑删除 |
| role | String | 角色(ROLE_USER/ROLE_ADMIN) |
| createTime | LocalDateTime | 创建时间 |

### Products 商品
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 商品名 |
| description | String | 描述 |
| price | BigDecimal | 价格 |
| imageUrl | String | 图片 |
| typeId | Long | 分类ID |
| status | Integer | 状态 |
| detailView | Integer | 浏览量 |
| userId | Long | 卖家ID |
| embedding | float[] | 1024维向量(AI) |

### Favorites 收藏
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| userId | Long | 用户ID |
| productId | Long | 商品ID |
| typeId | Long | 分类ID |
| createdAt | LocalDateTime | 收藏时间 |

### Transactionrecords 订单
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| productId | Long | 商品ID |
| buyerId | Long | 买家ID |
| sellerId | Long | 卖家ID |
| price | BigDecimal | 成交价 |
| status | Integer | 状态码 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |
