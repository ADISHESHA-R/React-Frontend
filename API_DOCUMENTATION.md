# HSK Shopping - Complete API Documentation

**Base URL:** 
- Local: `http://localhost:8082`
- Production: `https://your-app.onrender.com` (Update with your actual Render URL)

**Authentication:** JWT Token (Bearer Token)
- Get token by logging in via `/api/v1/auth/login`, `/api/v1/seller/login`, or `/api/v1/admin/login`
- Include token in header: `Authorization: Bearer <your_token>`

**Response Format:**
```json
{
  "success": true/false,
  "message": "Optional message",
  "data": { ... }
}
```

---

## 📦 PRODUCTS (Public - No Auth Required)

### 1. Get All Products
- **Method:** `GET`
- **URL:** `/api/v1/products`
- **Auth:** None
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Product Name",
      "description": "Product description",
      "price": 999.99,
      "category": "Electronics",
      "uniqueProductId": "PROD-123",
      "imageUrl": "/product-image/1"
    }
  ]
}
```

### 2. Get Product by ID
- **Method:** `GET`
- **URL:** `/api/v1/products/{id}`
- **Auth:** None
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Product Name",
    "description": "Product description",
    "price": 999.99,
    "category": "Electronics",
    "uniqueProductId": "PROD-123",
    "imageUrl": "/product-image/1"
  }
}
```

### 3. Get Products by Category
- **Method:** `GET`
- **URL:** `/api/v1/products/category/{category}`
- **Auth:** None
- **Example:** `/api/v1/products/category/Electronics`
- **Response:** Same as Get All Products

### 4. Search Products
- **Method:** `GET`
- **URL:** `/api/v1/products/search?query={keyword}`
- **Auth:** None
- **Example:** `/api/v1/products/search?query=laptop`
- **Response:** Same as Get All Products

### 5. Get Product Image
- **Method:** `GET`
- **URL:** `/product-image/{id}`
- **Auth:** None
- **Response:** Binary image data (JPEG/PNG)

---

## 🔐 AUTHENTICATION (Public - No Auth Required)

### 6. User Login
- **Method:** `POST`
- **URL:** `/api/v1/auth/login`
- **Auth:** None
- **Request Body:**
```json
{
  "username": "testuser",
  "password": "Test@1234"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "user": {
      "id": 1,
      "username": "testuser",
      "phoneNumber": "1234567890",
      "alternateNumber": "0987654321",
      "address": "123 Main St",
      "photoBase64": "base64_encoded_image"
    }
  }
}
```

### 7. User Signup
- **Method:** `POST`
- **URL:** `/api/v1/auth/signup`
- **Auth:** None
- **Request Body:**
```json
{
  "username": "newuser",
  "password": "Test@1234",
  "phoneNumber": "1234567890",
  "address": "123 Main St"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "newuser",
    "phoneNumber": "1234567890",
    "address": "123 Main St"
  }
}
```

### 8. Seller Login
- **Method:** `POST`
- **URL:** `/api/v1/seller/login`
- **Auth:** None
- **Request Body:**
```json
{
  "username": "testseller",
  "password": "Test@1234"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "seller": {
      "id": 1,
      "username": "testseller",
      "email": "seller@example.com",
      "whatsappNumber": "1234567890",
      "businessEmail": "business@example.com",
      "gstNumber": "GST123456",
      "photoBase64": "base64_encoded_image"
    }
  }
}
```

### 9. Seller Signup
- **Method:** `POST`
- **URL:** `/api/v1/seller/signup`
- **Auth:** None
- **Request Body:** `multipart/form-data`
  - `username`: string
  - `password`: string
  - `email`: string
  - `whatsappNumber`: string
  - `businessEmail`: string
  - `gstNumber`: string
  - `photo`: file (optional)
- **Response:**
```json
{
  "success": true,
  "message": "Seller registered successfully",
  "data": {
    "id": 1,
    "username": "newseller",
    "email": "seller@example.com",
    ...
  }
}
```

### 10. Admin Login
- **Method:** `POST`
- **URL:** `/api/v1/admin/login`
- **Auth:** None
- **Request Body:**
```json
{
  "username": "AdisheshaR",
  "password": "ADI@28RSCA"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "AdisheshaR",
    "roles": ["ROLE_ADMIN"]
  }
}
```

### 11. Get Current User (Auth Required)
- **Method:** `GET`
- **URL:** `/api/v1/auth/me`
- **Auth:** Required (USER role)
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
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

---

## 👤 USER ENDPOINTS (Requires USER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 12. Get User Profile
- **Method:** `GET`
- **URL:** `/api/v1/user/profile`
- **Response:**
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

### 13. Update User Profile
- **Method:** `PUT`
- **URL:** `/api/v1/user/profile`
- **Request Body:** `multipart/form-data`
  - `alternateNumber`: string (optional)
  - `address`: string (optional)
  - `photo`: file (optional)
- **Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    ...
  }
}
```

### 14. Get User Home Data
- **Method:** `GET`
- **URL:** `/api/v1/user/home`
- **Response:**
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

---

## 🛒 CART ENDPOINTS (Requires USER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 15. Get Cart
- **Method:** `GET`
- **URL:** `/api/v1/cart`
- **Response:**
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

### 16. Add to Cart
- **Method:** `POST`
- **URL:** `/api/v1/cart/add/{productId}?quantity={qty}`
- **Example:** `/api/v1/cart/add/1?quantity=2`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Item added to cart"
}
```

### 17. Remove from Cart
- **Method:** `DELETE`
- **URL:** `/api/v1/cart/remove/{productId}`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Item removed from cart"
}
```

### 18. Update Cart Quantity
- **Method:** `PUT`
- **URL:** `/api/v1/cart/update/{productId}?quantity={qty}`
- **Example:** `/api/v1/cart/update/1?quantity=5`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Cart updated"
}
```

---

## 💳 PAYMENT & ORDERS (Requires USER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 19. Buy Now - Get Product Details
- **Method:** `GET`
- **URL:** `/api/v1/payment/buy-now/{productId}?quantity={qty}`
- **Example:** `/api/v1/payment/buy-now/1?quantity=1`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "product": { ... },
    "quantity": 1,
    "amount": 999.99,
    "needsAddress": false
  }
}
```

### 20. Save Address (Buy Now Flow)
- **Method:** `POST`
- **URL:** `/api/v1/payment/buy-now/address`
- **Request Body:**
```json
{
  "address": "789 Payment St"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Address saved successfully"
}
```

### 21. Create Razorpay Order
- **Method:** `POST`
- **URL:** `/api/v1/payment/create-order`
- **Request Body:**
```json
{
  "amount": 50000
}
```
- **Response:**
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

### 22. Handle Payment Success
- **Method:** `POST`
- **URL:** `/api/v1/payment/success`
- **Request Body:**
```json
{
  "razorpay_payment_id": "pay_123456",
  "razorpay_order_id": "order_123456",
  "razorpay_signature": "signature_123456",
  "amount": 50000,
  "isBuyNow": false
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Payment successful"
}
```

### 23. Get User Orders
- **Method:** `GET`
- **URL:** `/api/v1/payment/orders`
- **Response:**
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

---

## 🏪 SELLER ENDPOINTS (Requires SELLER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 24. Get Seller Profile
- **Method:** `GET`
- **URL:** `/api/v1/seller/profile`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "testseller",
    "email": "seller@example.com",
    "whatsappNumber": "1234567890",
    "businessEmail": "business@example.com",
    "gstNumber": "GST123456",
    "photoBase64": "base64_encoded_image"
  }
}
```

### 25. Update Seller Profile
- **Method:** `PUT`
- **URL:** `/api/v1/seller/profile`
- **Request Body:** `multipart/form-data`
  - `whatsappNumber`: string (optional)
  - `businessEmail`: string (optional)
  - `gstNumber`: string (optional)
  - `photo`: file (optional)
- **Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": { ... }
}
```

### 26. Get Seller Dashboard
- **Method:** `GET`
- **URL:** `/api/v1/seller/dashboard`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": ["Electronics", "Clothing", "Books", ...]
}
```

### 27. Upload Product
- **Method:** `POST`
- **URL:** `/api/v1/seller/products`
- **Request Body:** `multipart/form-data`
  - `productName`: string
  - `productDescription`: string
  - `productPrice`: number
  - `productCategory`: string
  - `uniqueProductId`: string (optional)
  - `productImage`: file
- **Response:**
```json
{
  "success": true,
  "message": "Product uploaded successfully",
  "data": {
    "id": 1,
    "name": "New Product",
    "price": 999.99,
    "category": "Electronics",
    "imageUrl": "/product-image/1"
  }
}
```

### 28. Get My Products
- **Method:** `GET`
- **URL:** `/api/v1/seller/products`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "My Product",
      "price": 999.99,
      "category": "Electronics",
      "imageUrl": "/product-image/1"
    }
  ]
}
```

### 29. Get Seller Home
- **Method:** `GET`
- **URL:** `/api/v1/seller/home`
- **Response:** Same as Get Seller Profile

---

## 🔐 ADMIN ENDPOINTS (Requires ADMIN Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

**Admin Credentials:**
- Username: `AdisheshaR`
- Password: `ADI@28RSCA`

### 30. Get All Users
- **Method:** `GET`
- **URL:** `/api/v1/admin/users`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "username": "user1",
      "phoneNumber": "1234567890",
      "address": "123 Main St",
      "photoBase64": "base64_encoded_image"
    }
  ]
}
```

### 31. Update User
- **Method:** `PUT`
- **URL:** `/api/v1/admin/users/{id}`
- **Request Body:**
```json
{
  "username": "updateduser",
  "phoneNumber": "1111111111",
  "alternateNumber": "2222222222",
  "address": "Updated Address"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": { ... }
}
```

### 32. Delete User
- **Method:** `DELETE`
- **URL:** `/api/v1/admin/users/{id}`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "User deleted successfully"
}
```

### 33. Get All Sellers
- **Method:** `GET`
- **URL:** `/api/v1/admin/sellers`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "username": "seller1",
      "email": "seller@example.com",
      "gstNumber": "GST123456",
      ...
    }
  ]
}
```

### 34. Update Seller
- **Method:** `PUT`
- **URL:** `/api/v1/admin/sellers/{id}`
- **Request Body:**
```json
{
  "username": "updatedseller",
  "email": "newemail@example.com",
  "whatsappNumber": "3333333333",
  "businessEmail": "newbusiness@example.com",
  "gstNumber": "GST999999"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Seller updated successfully",
  "data": { ... }
}
```

### 35. Delete Seller
- **Method:** `DELETE`
- **URL:** `/api/v1/admin/sellers/{id}`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Seller deleted successfully"
}
```

### 36. Get All Products
- **Method:** `GET`
- **URL:** `/api/v1/admin/products`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Product Name",
      "price": 999.99,
      "category": "Electronics",
      "uniqueProductId": "PROD-123",
      "imageUrl": "/product-image/1"
    }
  ]
}
```

### 37. Update Product
- **Method:** `PUT`
- **URL:** `/api/v1/admin/products/{id}`
- **Request Body:**
```json
{
  "name": "Updated Product Name",
  "description": "Updated description",
  "price": 1299.99,
  "category": "Electronics",
  "uniqueProductId": "PROD-UPDATED"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "Product updated successfully",
  "data": { ... }
}
```

### 38. Delete Product
- **Method:** `DELETE`
- **URL:** `/api/v1/admin/products/{id}`
- **Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Product deleted successfully"
}
```

---

## 🔑 AUTHENTICATION FLOW

### Step 1: Login
```bash
POST /api/v1/auth/login
{
  "username": "testuser",
  "password": "Test@1234"
}
```

### Step 2: Get Token
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### Step 3: Use Token
```bash
GET /api/v1/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📊 HTTP STATUS CODES

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Not authenticated / Invalid/expired token
- `403 Forbidden` - Authenticated but not authorized (wrong role)
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## ⚠️ ERROR RESPONSES

### Missing Token
```json
{
  "success": false,
  "message": "JWT token is missing",
  "data": null
}
```

### Invalid Token
```json
{
  "success": false,
  "message": "Invalid or expired JWT token",
  "data": null
}
```

### Access Denied
```json
{
  "success": false,
  "message": "Forbidden: Insufficient permissions",
  "data": null
}
```

### Validation Error
```json
{
  "success": false,
  "message": "Username already exists",
  "data": null
}
```

---

## 🧪 QUICK TEST EXAMPLES

### Using curl:

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@1234"}' \
  | jq -r '.data.token')

# 2. Get Profile
curl -X GET http://localhost:8082/api/v1/user/profile \
  -H "Authorization: Bearer $TOKEN"

# 3. Add to Cart
curl -X POST "http://localhost:8082/api/v1/cart/add/1?quantity=2" \
  -H "Authorization: Bearer $TOKEN"
```

### Using JavaScript (Fetch API):

```javascript
// Login
const loginResponse = await fetch('http://localhost:8082/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'testuser', password: 'Test@1234' })
});
const loginData = await loginResponse.json();
const token = loginData.data.token;

// Get Profile
const profileResponse = await fetch('http://localhost:8082/api/v1/user/profile', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const profileData = await profileResponse.json();
```

---

## 📋 ENDPOINT SUMMARY

| # | Method | Endpoint | Auth | Role |
|---|--------|----------|------|------|
| 1 | GET | `/api/v1/products` | None | Public |
| 2 | GET | `/api/v1/products/{id}` | None | Public |
| 3 | GET | `/api/v1/products/category/{category}` | None | Public |
| 4 | GET | `/api/v1/products/search?query={query}` | None | Public |
| 5 | GET | `/product-image/{id}` | None | Public |
| 6 | POST | `/api/v1/auth/login` | None | Public |
| 7 | POST | `/api/v1/auth/signup` | None | Public |
| 8 | POST | `/api/v1/seller/login` | None | Public |
| 9 | POST | `/api/v1/seller/signup` | None | Public |
| 10 | POST | `/api/v1/admin/login` | None | Public |
| 11 | GET | `/api/v1/auth/me` | Required | USER |
| 12 | GET | `/api/v1/user/profile` | Required | USER |
| 13 | PUT | `/api/v1/user/profile` | Required | USER |
| 14 | GET | `/api/v1/user/home` | Required | USER |
| 15 | GET | `/api/v1/cart` | Required | USER |
| 16 | POST | `/api/v1/cart/add/{id}` | Required | USER |
| 17 | DELETE | `/api/v1/cart/remove/{id}` | Required | USER |
| 18 | PUT | `/api/v1/cart/update/{id}` | Required | USER |
| 19 | GET | `/api/v1/payment/buy-now/{id}` | Required | USER |
| 20 | POST | `/api/v1/payment/buy-now/address` | Required | USER |
| 21 | POST | `/api/v1/payment/create-order` | Required | USER |
| 22 | POST | `/api/v1/payment/success` | Required | USER |
| 23 | GET | `/api/v1/payment/orders` | Required | USER |
| 24 | GET | `/api/v1/seller/profile` | Required | SELLER |
| 25 | PUT | `/api/v1/seller/profile` | Required | SELLER |
| 26 | GET | `/api/v1/seller/dashboard` | Required | SELLER |
| 27 | POST | `/api/v1/seller/products` | Required | SELLER |
| 28 | GET | `/api/v1/seller/products` | Required | SELLER |
| 29 | GET | `/api/v1/seller/home` | Required | SELLER |
| 30 | GET | `/api/v1/admin/users` | Required | ADMIN |
| 31 | PUT | `/api/v1/admin/users/{id}` | Required | ADMIN |
| 32 | DELETE | `/api/v1/admin/users/{id}` | Required | ADMIN |
| 33 | GET | `/api/v1/admin/sellers` | Required | ADMIN |
| 34 | PUT | `/api/v1/admin/sellers/{id}` | Required | ADMIN |
| 35 | DELETE | `/api/v1/admin/sellers/{id}` | Required | ADMIN |
| 36 | GET | `/api/v1/admin/products` | Required | ADMIN |
| 37 | PUT | `/api/v1/admin/products/{id}` | Required | ADMIN |
| 38 | DELETE | `/api/v1/admin/products/{id}` | Required | ADMIN |

**Total: 38 API Endpoints**

---

## 🔒 SECURITY NOTES

1. **JWT Token Expiration:** 24 hours (configurable)
2. **Token Storage:** Store in `localStorage` or httpOnly cookie
3. **Password Requirements:** 
   - Minimum 8 characters
   - At least one uppercase, lowercase, digit, and special character
4. **CORS:** Configured for `http://localhost:3000` and `http://localhost:5173`
5. **CSRF:** Disabled for API endpoints (JWT is CSRF-resistant)

---

**Last Updated:** 2026-02-16
**Version:** 1.0
