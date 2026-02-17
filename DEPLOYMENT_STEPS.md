# 🚀 Deployment Steps for Render

## Prerequisites
1. ✅ Code is ready (API-only, cleaned up)
2. ✅ Database `shopping-api-db` created in Render
3. ✅ GitHub repository connected to Render

## Step-by-Step Deployment

### Step 1: Push Code to GitHub
```bash
git add .
git commit -m "API-only deployment ready - cleaned up web templates and controllers"
git push origin main
```

### Step 2: Create Web Service in Render

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository
4. Configure the service:

   **Basic Settings:**
   - **Name:** `shopping-app` (or your preferred name)
   - **Region:** `Oregon (US West)` (to match your database)
   - **Branch:** `main` (or your default branch)
   - **Root Directory:** `shopping` ⚠️ **IMPORTANT: Set this to `shopping`**
   - **Runtime:** `Java`
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar -Dspring.profiles.active=prod target/shopping-0.0.1-SNAPSHOT.jar`

   **OR** if Render auto-detects `render.yaml`:
   - It will use the settings from `shopping/render.yaml`
   - Make sure **Root Directory** is set to `shopping`

### Step 3: Link Database

1. In your web service settings, go to **"Environment"** tab
2. Find `DATABASE_URL` in the environment variables
3. Click the dropdown next to it
4. Select **"Link shopping-api-db"** (your database)
5. This will automatically set the `DATABASE_URL`

### Step 4: Set Environment Variables

Go to **Environment** tab and set these:

**Required:**
- `JWT_SECRET` - Generate a strong secret:
  ```bash
  openssl rand -base64 32
  ```
  Or use: `HSK_Shopping_Secret_Key_For_JWT_Token_Generation_2024_AdisheshaR_Production_Change_This`

- `CORS_ALLOWED_ORIGINS` - Your React app URLs (comma-separated):
  ```
  http://localhost:3000,http://localhost:5173,https://your-react-app.vercel.app
  ```

- `ADMIN_PASSWORD` - Your admin password (e.g., `ADI@28RSCA`)

**Optional (if using):**
- `RAZORPAY_KEY` - Your Razorpay key
- `RAZORPAY_SECRET` - Your Razorpay secret
- `GITHUB_CLIENT_SECRET` - If using GitHub OAuth
- `GOOGLE_CLIENT_ID` - If using Google OAuth
- `GOOGLE_CLIENT_SECRET` - If using Google OAuth

### Step 5: Deploy

1. Click **"Create Web Service"** or **"Save Changes"**
2. Render will:
   - Clone your repository
   - Build the application (`mvn clean package`)
   - Start the service
3. Watch the logs for any errors

### Step 6: Verify Deployment

Once deployed, test your API:

1. **Public endpoint (no auth):**
   ```
   https://your-app.onrender.com/api/v1/products
   ```

2. **Login endpoint:**
   ```
   POST https://your-app.onrender.com/api/v1/auth/login
   Content-Type: application/json
   
   {
     "username": "testuser",
     "password": "Test@1234"
   }
   ```

3. **Get token from response and test protected endpoint:**
   ```
   GET https://your-app.onrender.com/api/v1/user/home
   Authorization: Bearer YOUR_TOKEN_HERE
   ```

## Troubleshooting

### Build Fails
- Check build logs in Render
- Verify `pom.xml` is correct
- Ensure Java 17 is available

### Database Connection Fails
- Verify `DATABASE_URL` is set correctly
- Check database is in same region (Oregon)
- Verify database is "Available" status

### 401 Unauthorized
- Check `JWT_SECRET` is set
- Verify token is valid (not expired)
- Check token format (should start with `eyJ`)

### CORS Errors
- Update `CORS_ALLOWED_ORIGINS` with your React app URL
- Restart the service after updating

## Important Notes

- **Root Directory:** Must be set to `shopping` in Render dashboard
- **Database:** Already created as `shopping-api-db` - just link it
- **First Deploy:** May take 5-10 minutes
- **Cold Starts:** Free tier spins down after 15 min inactivity (first request may be slow)

## Your App URL

After deployment, your API will be available at:
```
https://your-app-name.onrender.com
```

Update this in:
- `API_DOCUMENTATION.md` (base URL)
- React frontend (API base URL)
- `CORS_ALLOWED_ORIGINS` environment variable
