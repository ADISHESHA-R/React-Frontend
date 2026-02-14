# ⚡ RENDER.COM QUICK START - 5 Simple Steps

## 🚀 Deploy Your Shopping App on Render in 30 Minutes

---

## **STEP 1: Get Your Keys** ⏱️ 10 min

### Razorpay Keys:
1. Go to: https://dashboard.razorpay.com/app/keys
2. Click **"Generate Live Keys"**
3. Copy:
   - Key ID: `rzp_live_xxxxxxxxxxxx`
   - Secret: `xxxxxxxxxxxxxxxxxxxx`

### Admin Password:
- Create: `MyStr0ng!P@ssw0rd123` (min 12 chars)

---

## **STEP 2: Create PostgreSQL** ⏱️ 5 min

1. Go to: https://render.com → Sign up/Login
2. Click **"New +"** → **"PostgreSQL"**
3. Fill:
   - Name: `shopping-db`
   - Database: `shopping_db`
   - User: `shopping_user`
   - Plan: **Free**
4. Click **"Create Database"**
5. Wait 2 min → Copy **"Internal Database URL"**

---

## **STEP 3: Create Web Service** ⏱️ 5 min

1. Click **"New +"** → **"Web Service"**
2. Connect GitHub → Select your repo
3. Fill:
   - Name: `shopping-app`
   - Root Directory: `shopping` (if code is in shopping folder)
   - Build: `mvn clean package -DskipTests`
   - Start: `java -jar -Dspring.profiles.active=prod target/shopping-0.0.1-SNAPSHOT.jar`

---

## **STEP 4: Set Environment Variables** ⏱️ 5 min

In Web Service settings, add these:

| Variable | Value |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | (Paste from Step 2) |
| `RAZORPAY_KEY` | (From Step 1) |
| `RAZORPAY_SECRET` | (From Step 1) |
| `ADMIN_USERNAME` | `admin` |
| `ADMIN_PASSWORD` | (From Step 1) |
| `GITHUB_CLIENT_ID` | `Ov23liCogGkwyc6V02jL` |

---

## **STEP 5: Deploy & Wait** ⏱️ 10-15 min

1. Click **"Create Web Service"**
2. Watch logs
3. Wait for: `Started ShoppingApplication`
4. Your app: `https://shopping-app.onrender.com`

---

## ✅ **DONE!**

Your app is live! 🎉

**Next:** Update OAuth URLs with your actual domain.

---

## 📚 **Need Details?**

Read: `RENDER_DEPLOYMENT_GUIDE.md` for complete step-by-step instructions.

**That's it! Follow these 5 steps and you're deployed! 🚀**
