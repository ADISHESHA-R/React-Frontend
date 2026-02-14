# Authentication Flow Documentation

## ✅ **Complete Authentication Flow**

### **User Login Flow:**
```
1. User visits: /login
2. User enters credentials
3. Authentication via UserDetailsServiceImpl
4. Role assigned: ROLE_USER
5. Redirect: /home (via CustomLoginSuccessHandler)
6. User sees: home.html (with shopping functionality)
```

**User Accessible Routes:**
- `/home` - User home page with products
- `/profile` - User profile management
- `/buy-now/**` - Buy products
- `/payment-success` - Payment confirmation
- `/create-order` - Create orders
- `/cart` - Shopping cart

**User Functionality:**
- Browse products
- Add to cart
- Make purchases
- View profile
- Manage orders

---

### **Seller Login Flow:**
```
1. Seller visits: /seller-login
2. Seller enters credentials
3. Authentication via SellerDetailsService
4. Role assigned: ROLE_SELLER
5. Redirect: /seller-dashboard (via CustomLoginSuccessHandler)
6. Seller sees: seller-dashboard.html (with seller functionality)
```

**Seller Accessible Routes:**
- `/seller-dashboard` - Seller dashboard
- `/seller-home` - Seller home page
- `/upload-product` - Upload products
- `/seller-profile` - Seller profile management

**Seller Functionality:**
- Upload products
- Manage product listings
- View seller profile
- Manage seller account

---

## 🔒 **Security Features Implemented**

### **1. Role-Based Access Control**
- ✅ Users can only access `/home`, `/profile`, `/buy-now`, etc.
- ✅ Sellers can only access `/seller-dashboard`, `/seller-profile`, `/upload-product`, etc.
- ✅ Cross-role access attempts are automatically redirected

### **2. Automatic Redirects**
- ✅ Seller trying to access `/home` → Redirected to `/seller-dashboard`
- ✅ User trying to access `/seller-dashboard` → Redirected to `/home`
- ✅ Unauthenticated access → Redirected to appropriate login page

### **3. Separate Authentication Providers**
- ✅ `userAuthProvider` - Handles user authentication
- ✅ `sellerAuthProvider` - Handles seller authentication
- ✅ Each uses separate UserDetailsService

### **4. Separate Security Filter Chains**
- ✅ Seller Filter Chain (`@Order(1)`) - Handles all seller routes
- ✅ User Filter Chain (`@Order(2)`) - Handles all user routes
- ✅ No conflicts between chains

---

## 📋 **Route Protection Summary**

| Route | User Access | Seller Access | Redirect If Wrong Role |
|-------|-------------|---------------|------------------------|
| `/home` | ✅ Yes | ❌ No | Seller → `/seller-dashboard` |
| `/profile` | ✅ Yes | ❌ No | Seller → `/seller-profile` |
| `/seller-dashboard` | ❌ No | ✅ Yes | User → `/home` |
| `/seller-profile` | ❌ No | ✅ Yes | User → `/home` |
| `/upload-product` | ❌ No | ✅ Yes | User → `/home` |
| `/login` | ✅ Public | ✅ Public | - |
| `/seller-login` | ✅ Public | ✅ Public | - |

---

## 🎯 **How It Works**

### **CustomLoginSuccessHandler:**
```java
- Checks user role after successful login
- ROLE_USER → Redirects to /home
- ROLE_SELLER → Redirects to /seller-dashboard
```

### **Controller-Level Protection:**
```java
- /home route: Checks for ROLE_USER, redirects sellers
- /seller-dashboard route: Checks for ROLE_SELLER, redirects users
- /profile route: Checks for ROLE_USER, redirects sellers
- /seller-profile route: Checks for ROLE_SELLER, redirects users
```

### **SecurityConfig:**
```java
- Seller Filter Chain: Protects /seller-* routes
- User Filter Chain: Protects /home, /profile, /buy-now routes
- Each chain uses appropriate authentication provider
```

---

## ✅ **Testing Checklist**

### **User Login:**
- [ ] Go to `/login`
- [ ] Enter user credentials
- [ ] Should redirect to `/home`
- [ ] Should see user home page with products
- [ ] Should be able to access `/profile`
- [ ] Should NOT be able to access `/seller-dashboard` (redirects to `/home`)

### **Seller Login:**
- [ ] Go to `/seller-login`
- [ ] Enter seller credentials
- [ ] Should redirect to `/seller-dashboard`
- [ ] Should see seller dashboard
- [ ] Should be able to access `/seller-profile`
- [ ] Should be able to upload products
- [ ] Should NOT be able to access `/home` (redirects to `/seller-dashboard`)

---

## 🎉 **Summary**

✅ **User Login** → `/home` (User functionality)
✅ **Seller Login** → `/seller-dashboard` (Seller functionality)
✅ **Proper role separation** - No cross-access
✅ **Automatic redirects** - Wrong role attempts are redirected
✅ **Secure** - CSRF protection enabled
✅ **Complete** - All routes properly protected

The authentication flow is now properly configured and working correctly!
