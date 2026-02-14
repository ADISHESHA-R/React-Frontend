# 🚀 RENDER.COM DEPLOYMENT GUIDE - Step by Step

## Complete Guide to Deploy Your Shopping App on Render

---

## 📋 **STEP 1: PREPARE YOUR API KEYS** (10 minutes)

### **1.1 Get Razorpay LIVE Keys**
1. Go to: https://dashboard.razorpay.com
2. Login to your Razorpay account
3. Click on **"Settings"** (left sidebar)
4. Click on **"API Keys"**
5. Click **"Generate Live Keys"** button
   - ⚠️ **IMPORTANT:** Use LIVE keys (`rzp_live_...`), NOT test keys!
6. Copy these two values:
   - **Key ID:** `rzp_live_xxxxxxxxxxxx` (starts with `rzp_live_`)
   - **Key Secret:** `xxxxxxxxxxxxxxxxxxxx` (long string)
7. **Save these securely** - you'll need them in Step 4

### **1.2 Create Strong Admin Password**
- Create a strong password (minimum 12 characters)
- Example: `MyStr0ng!P@ssw0rd123`
- Must include: uppercase, lowercase, numbers, symbols
- **Save this password** - you'll need it in Step 4

### **1.3 Get GitHub OAuth Secret (Optional)**
1. Go to: https://github.com/settings/developers
2. Click **"OAuth Apps"** → **"New OAuth App"**
3. Fill in:
   - **Application name:** Shopping App
   - **Homepage URL:** `https://your-app-name.onrender.com` (we'll update this later)
   - **Authorization callback URL:** `https://your-app-name.onrender.com/login/oauth2/code/github`
4. Click **"Register application"**
5. Copy the **Client Secret** (generate new one if needed)
6. **Save it** - you'll need it in Step 4

---

## 🌐 **STEP 2: CREATE RENDER ACCOUNT** (2 minutes)

1. Go to: https://render.com
2. Click **"Get Started for Free"** (top right)
3. Choose **"Sign up with GitHub"** (recommended) or email
4. Authorize Render to access your GitHub account
5. Verify your email if required

---

## 🗄️ **STEP 3: CREATE POSTGRESQL DATABASE** (5 minutes)

### **3.1 Create Database**
1. In Render dashboard, click the **"New +"** button (top right)
2. Select **"PostgreSQL"** from the dropdown
3. Fill in the form:
   - **Name:** `shopping-db`
   - **Database:** `shopping_db`
   - **User:** `shopping_user`
   - **Region:** Choose closest to you
     - For India: **Mumbai** or **Singapore**
     - For US: **Oregon** or **Ohio**
   - **PostgreSQL Version:** 15 (or latest available)
   - **Plan:** 
     - **Free** (for testing - spins down after inactivity)
     - **Starter** ($7/month - always on, recommended for production)
4. Click **"Create Database"**
5. **Wait 2-3 minutes** for database to be created

### **3.2 Get Database URL**
1. Once database is ready, click on **"shopping-db"** in your dashboard
2. Scroll down to **"Connections"** section
3. Find **"Internal Database URL"**
4. It looks like:
   ```
   postgresql://shopping_user:password@dpg-xxxxx-a.singapore-postgres.render.com/shopping_db
   ```
5. **Click the copy icon** next to it
6. **Save this URL** - you'll need it in Step 4!

---

## 🚀 **STEP 4: CREATE WEB SERVICE** (10 minutes)

### **4.1 Create Web Service**
1. In Render dashboard, click **"New +"** button again
2. Select **"Web Service"**
3. Click **"Connect account"** if you haven't connected GitHub
4. Select your GitHub repository: `razorpay-main` (or your repo name)
5. Fill in the form:

   **Basic Settings:**
   - **Name:** `shopping-app` (or any name you like)
   - **Region:** Same region as your database
   - **Branch:** `main` (or `master` if that's your branch)
   - **Root Directory:** `shopping` 
     - ⚠️ **IMPORTANT:** If your code is in a `shopping` folder, enter `shopping`
     - If code is in root, leave blank

   **Build & Deploy:**
   - **Environment:** Select **"Maven"** (or "Docker" if you prefer)
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar -Dspring.profiles.active=prod target/shopping-0.0.1-SNAPSHOT.jar`

### **4.2 Set Environment Variables**
Scroll down to **"Environment Variables"** section and click **"Add Environment Variable"** for each:

**Required Variables:**

1. **SPRING_PROFILES_ACTIVE**
   - Key: `SPRING_PROFILES_ACTIVE`
   - Value: `prod`
   - Click **"Save"**

2. **DATABASE_URL**
   - Key: `DATABASE_URL`
   - Value: Paste the PostgreSQL URL you copied in Step 3.2
   - Click **"Save"**

3. **RAZORPAY_KEY**
   - Key: `RAZORPAY_KEY`
   - Value: Your Razorpay Key ID from Step 1.1 (starts with `rzp_live_`)
   - Click **"Save"**

4. **RAZORPAY_SECRET**
   - Key: `RAZORPAY_SECRET`
   - Value: Your Razorpay Secret from Step 1.1
   - Click **"Save"**

5. **ADMIN_USERNAME**
   - Key: `ADMIN_USERNAME`
   - Value: `admin`
   - Click **"Save"**

6. **ADMIN_PASSWORD**
   - Key: `ADMIN_PASSWORD`
   - Value: Your strong password from Step 1.2
   - Click **"Save"**

**Optional Variables (for OAuth):**

7. **GITHUB_CLIENT_ID**
   - Key: `GITHUB_CLIENT_ID`
   - Value: `Ov23liCogGkwyc6V02jL`
   - Click **"Save"**

8. **GITHUB_CLIENT_SECRET**
   - Key: `GITHUB_CLIENT_SECRET`
   - Value: Your GitHub OAuth secret from Step 1.3
   - Click **"Save"**

9. **GOOGLE_CLIENT_ID** (if using Google login)
   - Key: `GOOGLE_CLIENT_ID`
   - Value: Your Google Client ID
   - Click **"Save"**

10. **GOOGLE_CLIENT_SECRET** (if using Google login)
    - Key: `GOOGLE_CLIENT_SECRET`
    - Value: Your Google Client Secret
    - Click **"Save"**

### **4.3 Deploy**
1. Scroll to the bottom
2. Click **"Create Web Service"** button
3. Render will start building your application
4. You'll see a progress screen with logs

---

## ⏳ **STEP 5: WAIT FOR DEPLOYMENT** (10-15 minutes)

### **5.1 Watch the Build Process**
1. You'll see the **"Logs"** tab showing build progress
2. Watch for these stages:
   - ✅ **"Cloning repository"** - Code is being pulled
   - ✅ **"Installing dependencies"** - Maven downloading packages
   - ✅ **"Building application"** - Compiling your code
   - ✅ **"Starting application"** - Your app is starting

### **5.2 Check for Success**
Look for this message in logs:
```
Started ShoppingApplication in X.XXX seconds
```

### **5.3 Get Your App URL**
Once deployed, you'll see:
- **Your app URL:** `https://shopping-app.onrender.com` (or your chosen name)
- Click the URL to open your application

---

## ✅ **STEP 6: VERIFY DEPLOYMENT** (5 minutes)

### **6.1 Check Application**
1. Visit your app URL: `https://shopping-app.onrender.com`
2. You should see your shopping application homepage
3. If you see an error, check the logs

### **6.2 Check Logs for Errors**
1. In Render dashboard, click on your web service
2. Go to **"Logs"** tab
3. Look for any red error messages
4. Common issues:
   - Database connection errors → Check `DATABASE_URL`
   - Missing environment variables → Check all variables are set
   - Build errors → Check build logs

### **6.3 Test Basic Functionality**
- [ ] Homepage loads
- [ ] User registration works
- [ ] User login works
- [ ] Products display
- [ ] Add to cart works
- [ ] **Buy Now button works** (POST request)

---

## 🔧 **STEP 7: UPDATE OAUTH REDIRECT URLs** (5 minutes)

After deployment, update OAuth apps with your actual Render URL:

### **7.1 Update GitHub OAuth**
1. Go to: https://github.com/settings/developers
2. Click on your OAuth App
3. Click **"Edit"**
4. Update:
   - **Homepage URL:** `https://shopping-app.onrender.com`
   - **Authorization callback URL:** `https://shopping-app.onrender.com/login/oauth2/code/github`
5. Click **"Update application"**

### **7.2 Update Google OAuth** (if using)
1. Go to: https://console.cloud.google.com
2. Navigate to **"APIs & Services"** → **"Credentials"**
3. Click on your OAuth Client
4. Add to **"Authorized redirect URIs":**
   - `https://shopping-app.onrender.com/login/oauth2/code/google`
5. Click **"Save"**

---

## 🧪 **STEP 8: TEST PAYMENT FLOW** (5 minutes)

### **8.1 Test Buy Now**
1. Login as a user
2. Browse products
3. Click **"Buy Now"** button
4. Verify payment gateway opens
5. Test with a small amount (if using test mode)

### **8.2 Verify Database**
1. In Render dashboard, go to your PostgreSQL database
2. Click **"Connect"** → **"psql"** (if available)
3. Or use a database tool to verify tables are created
4. Check that orders are being saved

---

## 📊 **STEP 9: MONITOR YOUR APPLICATION**

### **9.1 Check Logs Regularly**
- Go to your web service → **"Logs"** tab
- Monitor for errors
- Check application performance

### **9.2 Monitor Database**
- Check database usage
- Free tier has limits
- Upgrade if needed

### **9.3 Set Up Alerts** (Optional)
- In Render dashboard, set up email alerts
- Get notified of deployment failures
- Monitor application health

---

## 🆘 **TROUBLESHOOTING**

### **Problem: Build Failed**
**Solution:**
- Check build logs for errors
- Verify `Root Directory` is correct (`shopping` if code is in shopping folder)
- Check Maven build command is correct
- Verify all dependencies in `pom.xml`

### **Problem: Application Won't Start**
**Solution:**
- Check logs for startup errors
- Verify all environment variables are set
- Check `DATABASE_URL` is correct
- Verify Razorpay keys are LIVE (not test)

### **Problem: Database Connection Failed**
**Solution:**
- Verify `DATABASE_URL` is correct
- Check database is running (not spun down on free tier)
- Verify database credentials
- Check network connectivity

### **Problem: Payment Gateway Not Opening**
**Solution:**
- Verify Razorpay keys are LIVE (`rzp_live_...`)
- Check browser console for errors
- Verify CSRF token is being sent
- Check application logs

### **Problem: 404 Errors**
**Solution:**
- Verify `Root Directory` is set correctly
- Check build output for JAR file location
- Verify start command path is correct

---

## 📋 **QUICK REFERENCE**

### **Environment Variables Checklist:**
```
✅ SPRING_PROFILES_ACTIVE=prod
✅ DATABASE_URL=postgresql://user:pass@host:port/dbname
✅ RAZORPAY_KEY=rzp_live_xxxxxxxxxxxx
✅ RAZORPAY_SECRET=xxxxxxxxxxxxxxxxxxxx
✅ ADMIN_USERNAME=admin
✅ ADMIN_PASSWORD=YourStrongPassword123!
✅ GITHUB_CLIENT_ID=Ov23liCogGkwyc6V02jL (optional)
✅ GITHUB_CLIENT_SECRET=xxxxx (optional)
```

### **Important URLs:**
- Render Dashboard: https://dashboard.render.com
- Razorpay Dashboard: https://dashboard.razorpay.com
- GitHub OAuth: https://github.com/settings/developers
- Google Cloud: https://console.cloud.google.com

### **Your App URL:**
- After deployment: `https://shopping-app.onrender.com` (or your chosen name)

---

## ⏱️ **TIME ESTIMATE**

- Getting API keys: **10 minutes**
- Creating Render account: **2 minutes**
- Creating PostgreSQL: **5 minutes**
- Setting up Web Service: **10 minutes**
- Deployment wait: **10-15 minutes**
- Testing: **5 minutes**
- **Total: ~45 minutes**

---

## ✅ **DEPLOYMENT CHECKLIST**

Before you start:
- [ ] Razorpay LIVE keys ready
- [ ] Strong admin password created
- [ ] GitHub repository ready
- [ ] Render account created

During deployment:
- [ ] PostgreSQL database created
- [ ] Database URL copied
- [ ] Web service created
- [ ] All environment variables set
- [ ] Deployment started

After deployment:
- [ ] Application loads successfully
- [ ] No errors in logs
- [ ] OAuth URLs updated
- [ ] Payment flow tested
- [ ] Database connection verified

---

## 🎉 **YOU'RE DONE!**

Your application is now live on Render.com!

**Next Steps:**
1. Share your app URL with users
2. Monitor logs regularly
3. Upgrade to paid plan if needed (for always-on service)
4. Set up custom domain (optional)

**Congratulations on your deployment! 🚀**

---

## 📚 **NEED MORE HELP?**

- **Detailed PostgreSQL Guide:** See `STEP_BY_STEP_DEPLOYMENT.md`
- **Troubleshooting:** Check Render documentation
- **Support:** Render has excellent support - check their docs

**Good luck with your deployment!**
