# HSK Shopping API - Complete Handover Documentation

**For Frontend Developer**

---

## 📋 Table of Contents

1. [Quick Start](#quick-start)
2. [Authentication](#authentication)
3. [Public APIs](#public-apis)
4. [User APIs](#user-apis)
5. [Cart APIs](#cart-apis)
6. [Payment APIs](#payment-apis)
7. [Seller APIs](#seller-apis)
8. [Admin APIs](#admin-apis)
9. [Error Handling](#error-handling)
10. [Code Examples](#code-examples)

---

## 🚀 Quick Start

### Base URLs
- **Local Development:** `http://localhost:8082`
- **Production:** `https://react-frontend-9wcj.onrender.com`

### Response Format
All API responses follow this structure:
```json
{
  "success": true/false,
  "message": "Optional message",
  "data": { ... }
}
```

### Authentication
- **Type:** JWT Bearer Token
- **Get Token:** Login via `/api/v1/auth/login`
- **Use Token:** Include in header: `Authorization: Bearer <token>`
- **Expiration:** 24 hours

---

## 🔐 Authentication

### 1. User Login
**Endpoint:** `POST /api/v1/auth/login`

**Request:**
```json
{
  "username": "testuser",
  "password": "Test@1234"
}
```

**Response:**
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

### 2. User Signup
**Endpoint:** `POST /api/v1/auth/signup`

**Request:**
```json
{
  "username": "newuser",
  "password": "Test@1234",
  "phoneNumber": "1234567890",
  "address": "123 Main St"
}
```

**Response:**
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

### 3. Seller Login
**Endpoint:** `POST /api/v1/seller/login`

**Request:**
```json
{
  "username": "testseller",
  "password": "Test@1234"
}
```

**Response:**
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

### 4. Seller Signup
**Endpoint:** `POST /api/v1/seller/signup`

**Request:** `multipart/form-data` or JSON with `photoBase64`
```json
{
  "username": "newseller",
  "password": "Test@1234",
  "email": "seller@example.com",
  "whatsappNumber": "1234567890",
  "businessEmail": "business@example.com",
  "gstNumber": "GST123456",
  "photoBase64": "base64_encoded_image_string"
}
```

**Response:**
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

### 5. Admin Login
**Endpoint:** `POST /api/v1/admin/login`

**Request:**
```json
{
  "username": "admin",
  "password": "ADI@28adi"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "admin",
    "roles": ["ROLE_ADMIN"]
  }
}
```

---

## 📦 Public APIs (No Authentication Required)

### 6. Get All Products
**Endpoint:** `GET /api/v1/products`

**Response:**
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

### 7. Get Product by ID
**Endpoint:** `GET /api/v1/products/{id}`

**Response:**
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

### 8. Get Products by Category
**Endpoint:** `GET /api/v1/products/category/{category}`

**Example:** `/api/v1/products/category/Electronics`

**Response:** Same as Get All Products

### 9. Search Products
**Endpoint:** `GET /api/v1/products/search?query={keyword}`

**Example:** `/api/v1/products/search?query=laptop`

**Response:** Same as Get All Products

### 10. Get Product Image
**Endpoint:** `GET /product-image/{id}`

**Response:** Binary image data (JPEG/PNG)

**Usage in React:**
```jsx
<img src={`${API_BASE_URL}/product-image/${product.id}`} />
```

---

## 👤 User APIs (Requires USER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 11. Get Current User
**Endpoint:** `GET /api/v1/auth/me`

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

### 12. Get User Profile
**Endpoint:** `GET /api/v1/user/profile`

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

### 13. Update User Profile
**Endpoint:** `PUT /api/v1/user/profile`

**Request:** `multipart/form-data`
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

### 14. Get User Home Data
**Endpoint:** `GET /api/v1/user/home`

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

---

## 🛒 Cart APIs (Requires USER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 15. Get Cart
**Endpoint:** `GET /api/v1/cart`

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

### 16. Add to Cart
**Endpoint:** `POST /api/v1/cart/add/{productId}?quantity={qty}`

**Example:** `/api/v1/cart/add/1?quantity=2`

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Item added to cart"
}
```

### 17. Remove from Cart
**Endpoint:** `DELETE /api/v1/cart/remove/{productId}`

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Item removed from cart"
}
```

### 18. Update Cart Quantity
**Endpoint:** `PUT /api/v1/cart/update/{productId}?quantity={qty}`

**Example:** `/api/v1/cart/update/1?quantity=5`

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Cart updated"
}
```

---

## 💳 Payment & Orders APIs (Requires USER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 19. Buy Now - Get Product Details
**Endpoint:** `GET /api/v1/payment/buy-now/{productId}?quantity={qty}`

**Example:** `/api/v1/payment/buy-now/1?quantity=1`

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

### 20. Save Address (Buy Now Flow)
**Endpoint:** `POST /api/v1/payment/buy-now/address`

**Request:**
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

### 21. Create Razorpay Order
**Endpoint:** `POST /api/v1/payment/create-order`

**Request:**
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

### 22. Handle Payment Success
**Endpoint:** `POST /api/v1/payment/success`

**Request:**
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

### 23. Get User Orders
**Endpoint:** `GET /api/v1/payment/orders`

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

---

## 🏪 Seller APIs (Requires SELLER Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

### 24. Get Seller Profile
**Endpoint:** `GET /api/v1/seller/profile`

**Response:**
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
**Endpoint:** `PUT /api/v1/seller/profile`

**Request:** `multipart/form-data`
- `whatsappNumber`: string (optional)
- `businessEmail`: string (optional)
- `gstNumber`: string (optional)
- `photo`: file (optional)

**Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": { ... }
}
```

### 26. Get Seller Dashboard
**Endpoint:** `GET /api/v1/seller/dashboard`

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": ["Electronics", "Clothing", "Books", ...]
}
```

### 27. Upload Product
**Endpoint:** `POST /api/v1/seller/products`

**Request:** `multipart/form-data`
- `productName`: string
- `productDescription`: string
- `productPrice`: number
- `productCategory`: string
- `uniqueProductId`: string (optional)
- `productImage`: file

**Response:**
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
**Endpoint:** `GET /api/v1/seller/products`

**Response:**
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
**Endpoint:** `GET /api/v1/seller/home`

**Response:** Same as Get Seller Profile

---

## 🔐 Admin APIs (Requires ADMIN Role JWT Token)

**All endpoints require:** `Authorization: Bearer <token>`

**Admin Credentials:**
- Username: `admin`
- Password: `ADI@28adi`

### 30. Get All Users
**Endpoint:** `GET /api/v1/admin/users`

**Response:**
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
**Endpoint:** `PUT /api/v1/admin/users/{id}`

**Request:**
```json
{
  "username": "updateduser",
  "phoneNumber": "1111111111",
  "alternateNumber": "2222222222",
  "address": "Updated Address"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": { ... }
}
```

### 32. Delete User
**Endpoint:** `DELETE /api/v1/admin/users/{id}`

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "User deleted successfully"
}
```

### 33. Get All Sellers
**Endpoint:** `GET /api/v1/admin/sellers`

**Response:**
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
**Endpoint:** `PUT /api/v1/admin/sellers/{id}`

**Request:**
```json
{
  "username": "updatedseller",
  "email": "newemail@example.com",
  "whatsappNumber": "3333333333",
  "businessEmail": "newbusiness@example.com",
  "gstNumber": "GST999999"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Seller updated successfully",
  "data": { ... }
}
```

### 35. Delete Seller
**Endpoint:** `DELETE /api/v1/admin/sellers/{id}`

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Seller deleted successfully"
}
```

### 36. Get All Products (Admin)
**Endpoint:** `GET /api/v1/admin/products`

**Response:**
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
**Endpoint:** `PUT /api/v1/admin/products/{id}`

**Request:**
```json
{
  "name": "Updated Product Name",
  "description": "Updated description",
  "price": 1299.99,
  "category": "Electronics",
  "uniqueProductId": "PROD-UPDATED"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Product updated successfully",
  "data": { ... }
}
```

### 38. Delete Product
**Endpoint:** `DELETE /api/v1/admin/products/{id}`

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": "Product deleted successfully"
}
```

---

## ⚠️ Error Handling

### HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Not authenticated / Invalid/expired token
- `403 Forbidden` - Authenticated but not authorized (wrong role)
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
  "success": false,
  "message": "Error message here",
  "data": null
}
```

### Common Errors

#### Missing Token (401)
```json
{
  "success": false,
  "message": "JWT token is missing",
  "data": null
}
```

#### Invalid/Expired Token (401)
```json
{
  "success": false,
  "message": "Invalid or expired JWT token",
  "data": null
}
```

#### Access Denied (403)
```json
{
  "success": false,
  "message": "Forbidden: Insufficient permissions",
  "data": null
}
```

#### Validation Error (400)
```json
{
  "success": false,
  "message": "Username already exists",
  "data": null
}
```

---

## 💻 Code Examples

### JavaScript/Fetch API

#### Login and Store Token
```javascript
// Login
const loginResponse = await fetch('http://localhost:8082/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'testuser', password: 'Test@1234' })
});
const loginData = await loginResponse.json();
const token = loginData.data.token;

// Store token
localStorage.setItem('jwt_token', token);
```

#### Get User Profile
```javascript
const token = localStorage.getItem('jwt_token');

const response = await fetch('http://localhost:8082/api/v1/user/profile', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();
console.log(data);
```

#### Add to Cart
```javascript
const token = localStorage.getItem('jwt_token');

const response = await fetch('http://localhost:8082/api/v1/cart/add/1?quantity=2', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();
```

#### Update Profile with File Upload
```javascript
const token = localStorage.getItem('jwt_token');
const formData = new FormData();
formData.append('address', 'New Address');
formData.append('photo', fileInput.files[0]);

const response = await fetch('http://localhost:8082/api/v1/user/profile', {
  method: 'PUT',
  headers: { 'Authorization': `Bearer ${token}` },
  body: formData
});
const data = await response.json();
```

#### Upload Product (Seller)
```javascript
const token = localStorage.getItem('jwt_token');
const formData = new FormData();
formData.append('productName', 'New Product');
formData.append('productDescription', 'Product description');
formData.append('productPrice', '999.99');
formData.append('productCategory', 'Electronics');
formData.append('productImage', fileInput.files[0]);

const response = await fetch('http://localhost:8082/api/v1/seller/products', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` },
  body: formData
});
const data = await response.json();
```

### React Example

```jsx
import { useState, useEffect } from 'react';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('http://localhost:8082/api/v1/products')
      .then(res => res.json())
      .then(data => {
        if (data.success) {
          setProducts(data.data);
        }
        setLoading(false);
      })
      .catch(err => {
        console.error('Error:', err);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      {products.map(product => (
        <div key={product.id}>
          <img src={`http://localhost:8082${product.imageUrl}`} alt={product.name} />
          <h3>{product.name}</h3>
          <p>${product.price}</p>
        </div>
      ))}
    </div>
  );
}
```

### Axios Example

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8082',
});

// Add token to all requests
api.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Login
const login = async (username, password) => {
  const response = await api.post('/api/v1/auth/login', { username, password });
  localStorage.setItem('jwt_token', response.data.data.token);
  return response.data;
};

// Get profile
const getProfile = async () => {
  const response = await api.get('/api/v1/user/profile');
  return response.data;
};
```

---

## 📊 Complete Endpoint Summary

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

## 🔒 Security Notes

1. **JWT Token Expiration:** 24 hours
2. **Token Storage:** Store in `localStorage` or httpOnly cookie
3. **Password Requirements:** 
   - Minimum 8 characters
   - At least one uppercase, lowercase, digit, and special character
4. **CORS:** Configured for production React app URLs (set in Render environment variables)
5. **CSRF:** Disabled for API endpoints (JWT is CSRF-resistant)

---

## 📝 Important Notes

1. **Image URLs:** Product images use `/product-image/{id}` - prepend base URL in frontend
2. **Profile Photos:** Returned as Base64 strings in `photoBase64` field
3. **Amount Format:** Razorpay amounts are in paise (multiply by 100 for rupees)
4. **File Uploads:** Use `multipart/form-data` for file uploads
5. **Error Handling:** Always check `success` field before using `data`

---

**Last Updated:** 2026-02-16  
**Version:** 1.0  
**Production API Base URL:** `https://react-frontend-9wcj.onrender.com`
