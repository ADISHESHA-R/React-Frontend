# 🔐 Private APIs - User Login Required

**Base URL:** `http://localhost:8082` (or your deployed URL)

**Authentication:** All endpoints require JWT Bearer Token with **USER** role

**How to get token:**
1. Login via: `POST /api/v1/auth/login`
2. Copy the `token` from response
3. Include in header: `Authorization: Bearer <your_token>`

---

## 📋 Quick Reference

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 1 | GET | `/api/v1/auth/me` | Get current logged-in user |
| 2 | GET | `/api/v1/user/profile` | Get user profile |
| 3 | PUT | `/api/v1/user/profile` | Update user profile |
| 4 | GET | `/api/v1/user/home` | Get user home data |
| 5 | GET | `/api/v1/cart` | Get shopping cart |
| 6 | POST | `/api/v1/cart/add/{productId}` | Add item to cart |
| 7 | DELETE | `/api/v1/cart/remove/{productId}` | Remove item from cart |
| 8 | PUT | `/api/v1/cart/update/{productId}` | Update cart quantity |
| 9 | GET | `/api/v1/payment/buy-now/{productId}` | Get product for buy now |
| 10 | POST | `/api/v1/payment/buy-now/address` | Save address (buy now) |
| 11 | POST | `/api/v1/payment/create-order` | Create Razorpay order |
| 12 | POST | `/api/v1/payment/success` | Handle payment success |
| 13 | GET | `/api/v1/payment/orders` | Get user orders |

**Total: 13 Private Endpoints**

---

## 🔑 1. Get Current User

**Endpoint:** `GET /api/v1/auth/me`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "testuser",
    "phoneNumber": "1234567890",
    "alternateNumber": "0987654321",
    "address": "123 Main St",
    "photoBase64": "base64_encoded_image"
  }
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8082/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 👤 2. Get User Profile

**Endpoint:** `GET /api/v1/user/profile`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "testuser",
    "phoneNumber": "1234567890",
    "alternateNumber": "0987654321",
    "address": "123 Main St",
    "photoBase64": "base64_encoded_image"
  }
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8082/api/v1/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## ✏️ 3. Update User Profile

**Endpoint:** `PUT /api/v1/user/profile`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: multipart/form-data
```

**Request Body (form-data):**
- `alternateNumber`: string (optional)
- `address`: string (optional)
- `photo`: file (optional)

**Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    "phoneNumber": "1234567890",
    "alternateNumber": "0987654321",
    "address": "Updated Address",
    "photoBase64": "base64_encoded_image"
  }
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8082/api/v1/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "address=New Address" \
  -F "alternateNumber=9876543210" \
  -F "photo=@/path/to/image.jpg"
```

---

## 🏠 4. Get User Home Data

**Endpoint:** `GET /api/v1/user/home`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Home data retrieved successfully",
  "data": {
    "user": {
      "id": 1,
      "username": "testuser",
      "phoneNumber": "1234567890",
      "alternateNumber": "0987654321",
      "address": "123 Main St",
      "photoBase64": "base64_encoded_image"
    },
    "products": [
      {
        "id": 1,
        "name": "Product Name",
        "description": "Product description",
        "price": 999.99,
        "category": "Electronics",
        "uniqueProductId": "PROD-123",
        "imageUrl": "/product-image/1"
      }
    ],
    "categories": ["Electronics", "Clothing", "Books"]
  }
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8082/api/v1/user/home \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🛒 5. Get Shopping Cart

**Endpoint:** `GET /api/v1/cart`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "items": [
      {
        "product": {
          "id": 1,
          "name": "Product Name",
          "price": 999.99,
          "imageUrl": "/product-image/1"
        },
        "quantity": 2,
        "subtotal": 1999.98
      }
    ],
    "total": 1999.98
  }
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8082/api/v1/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## ➕ 6. Add to Cart

**Endpoint:** `POST /api/v1/cart/add/{productId}?quantity={qty}`

**Example:** `POST /api/v1/cart/add/1?quantity=2`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Item added to cart"
}
```

**cURL Example:**
```bash
curl -X POST "http://localhost:8082/api/v1/cart/add/1?quantity=2" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## ❌ 7. Remove from Cart

**Endpoint:** `DELETE /api/v1/cart/remove/{productId}`

**Example:** `DELETE /api/v1/cart/remove/1`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Item removed from cart"
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8082/api/v1/cart/remove/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔄 8. Update Cart Quantity

**Endpoint:** `PUT /api/v1/cart/update/{productId}?quantity={qty}`

**Example:** `PUT /api/v1/cart/update/1?quantity=5`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Cart updated"
}
```

**cURL Example:**
```bash
curl -X PUT "http://localhost:8082/api/v1/cart/update/1?quantity=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 💳 9. Buy Now - Get Product Details

**Endpoint:** `GET /api/v1/payment/buy-now/{productId}?quantity={qty}`

**Example:** `GET /api/v1/payment/buy-now/1?quantity=1`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "product": {
      "id": 1,
      "name": "Product Name",
      "price": 999.99,
      "category": "Electronics"
    },
    "quantity": 1,
    "amount": 999.99,
    "needsAddress": false
  }
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8082/api/v1/payment/buy-now/1?quantity=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📍 10. Save Address (Buy Now Flow)

**Endpoint:** `POST /api/v1/payment/buy-now/address`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "address": "789 Payment St"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Address saved successfully"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8082/api/v1/payment/buy-now/address \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"address": "789 Payment St"}'
```

---

## 💰 11. Create Razorpay Order

**Endpoint:** `POST /api/v1/payment/create-order`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": 50000
}
```
*Note: Amount is in paise (50000 = ₹500.00)*

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "order_123456",
    "amount": 50000,
    "key": "razorpay_key"
  }
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8082/api/v1/payment/create-order \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 50000}'
```

---

## ✅ 12. Handle Payment Success

**Endpoint:** `POST /api/v1/payment/success`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "razorpay_payment_id": "pay_123456",
  "razorpay_order_id": "order_123456",
  "razorpay_signature": "signature_123456",
  "amount": 50000,
  "isBuyNow": false
}
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Payment successful"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8082/api/v1/payment/success \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "razorpay_payment_id": "pay_123456",
    "razorpay_order_id": "order_123456",
    "razorpay_signature": "signature_123456",
    "amount": 50000,
    "isBuyNow": false
  }'
```

---

## 📦 13. Get User Orders

**Endpoint:** `GET /api/v1/payment/orders`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "razorpayOrderId": "order_123456",
      "razorpayPaymentId": "pay_123456",
      "amount": 500.00,
      "orderDate": "2026-02-16T10:30:00",
      "email": "user@example.com"
    }
  ]
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8082/api/v1/payment/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔄 Complete Authentication Flow

### Step 1: Login to Get Token
```bash
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@1234"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": { ... }
  }
}
```

### Step 2: Use Token for Private APIs
```bash
# Save token to variable (bash)
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Use in subsequent requests
curl -X GET http://localhost:8082/api/v1/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

---

## ⚠️ Error Responses

### Missing Token (401 Unauthorized)
```json
{
  "success": false,
  "message": "JWT token is missing",
  "data": null
}
```

### Invalid/Expired Token (401 Unauthorized)
```json
{
  "success": false,
  "message": "Invalid or expired JWT token",
  "data": null
}
```

### Wrong Role (403 Forbidden)
```json
{
  "success": false,
  "message": "Forbidden: Insufficient permissions",
  "data": null
}
```

---

## 📝 JavaScript/Fetch Examples

### Get User Profile
```javascript
const token = localStorage.getItem('jwt_token');

fetch('http://localhost:8082/api/v1/user/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

### Add to Cart
```javascript
const token = localStorage.getItem('jwt_token');

fetch('http://localhost:8082/api/v1/cart/add/1?quantity=2', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

### Update Profile
```javascript
const token = localStorage.getItem('jwt_token');
const formData = new FormData();
formData.append('address', 'New Address');
formData.append('photo', fileInput.files[0]);

fetch('http://localhost:8082/api/v1/user/profile', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## 🧪 Testing in Postman

1. **Create Environment Variable:**
   - Variable: `base_url`
   - Value: `http://localhost:8082`
   - Variable: `jwt_token`
   - Value: (leave empty, will be set after login)

2. **Login First:**
   - Method: `POST`
   - URL: `{{base_url}}/api/v1/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "testuser",
       "password": "Test@1234"
     }
     ```
   - Copy `data.token` from response
   - Set it in `jwt_token` variable

3. **Use Token in Private APIs:**
   - Go to Authorization tab
   - Type: `Bearer Token`
   - Token: `{{jwt_token}}`

---

## 📊 Summary

**Total Private Endpoints:** 13

**Categories:**
- **Authentication:** 1 endpoint (`/api/v1/auth/me`)
- **User Profile:** 3 endpoints (`/api/v1/user/*`)
- **Shopping Cart:** 4 endpoints (`/api/v1/cart/*`)
- **Payment & Orders:** 5 endpoints (`/api/v1/payment/*`)

**All require:**
- ✅ Valid JWT token
- ✅ USER role
- ✅ `Authorization: Bearer <token>` header

---

**Last Updated:** 2026-02-16
