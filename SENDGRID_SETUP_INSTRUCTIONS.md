# SendGrid Email Setup Instructions

## Overview
SendGrid has been integrated into the application to provide reliable email delivery. SendGrid works much better with cloud platforms like Render compared to Gmail SMTP.

## Free Tier
- **100 emails per day** - Forever free
- Perfect for OTP verification and low-volume email needs
- No credit card required

## Setup Steps

### Step 1: Create SendGrid Account
1. Go to https://sendgrid.com
2. Click "Start for Free"
3. Sign up with your email
4. Verify your email address
5. Complete the account setup

### Step 2: Create API Key
1. Log in to SendGrid dashboard
2. Go to **Settings** → **API Keys**
3. Click **Create API Key**
4. Name it: `Shopping App API Key`
5. Select **Full Access** or **Restricted Access** with **Mail Send** permission
6. Click **Create & View**
7. **Copy the API key immediately** (you won't be able to see it again!)

### Step 3: Verify Sender Email (Optional but Recommended)
1. Go to **Settings** → **Sender Authentication**
2. Click **Verify a Single Sender**
3. Fill in your details:
   - **From Email**: Your email (e.g., `noreply@yourdomain.com`)
   - **From Name**: Your app name (e.g., `Shopping Team`)
   - **Reply To**: Your email
   - **Company Address**: Your address
4. Click **Create**
5. Check your email and click the verification link

### Step 4: Configure Environment Variables in Render
1. Go to your Render Dashboard
2. Select your service
3. Go to **Environment** tab
4. Add the following environment variables:

```
EMAIL_PROVIDER=SENDGRID
SENDGRID_API_KEY=your-api-key-here
SENDGRID_FROM_EMAIL=noreply@yourdomain.com
```

**Important:**
- Replace `your-api-key-here` with the API key you copied in Step 2
- Replace `noreply@yourdomain.com` with your verified sender email (or use your Gmail if you verified it)

### Step 5: Deploy
1. The code is already updated
2. Render will automatically redeploy when you push to GitHub
3. Or manually trigger a deploy from Render dashboard

## Testing

After deployment, test the signup endpoint:
```bash
POST https://react-frontend-9wcj.onrender.com/api/v1/auth/signup
{
  "username": "testuser",
  "password": "Test@1234",
  "email": "your-email@gmail.com",
  "phoneNumber": "1234567890",
  "address": "123 Test St"
}
```

You should receive the OTP email in your inbox!

## Configuration Options

### Use SendGrid (Recommended)
```
EMAIL_PROVIDER=SENDGRID
SENDGRID_API_KEY=your-api-key
SENDGRID_FROM_EMAIL=noreply@yourdomain.com
```

### Use Gmail SMTP (Not recommended for Render)
```
EMAIL_PROVIDER=SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## Troubleshooting

### Emails not being sent?
1. Check Render logs for SendGrid errors
2. Verify API key is correct
3. Check if sender email is verified in SendGrid
4. Check SendGrid dashboard for email activity

### API Key Issues?
- Make sure the API key has "Mail Send" permission
- Regenerate the API key if needed
- Ensure no extra spaces in the environment variable

### Rate Limits?
- Free tier: 100 emails/day
- Check SendGrid dashboard for usage
- Upgrade plan if needed

## Benefits of SendGrid
✅ Works reliably with cloud platforms  
✅ Better deliverability than Gmail SMTP  
✅ Free tier sufficient for OTP verification  
✅ Easy to scale if needed  
✅ Detailed email analytics  

## Support
- SendGrid Documentation: https://docs.sendgrid.com
- SendGrid Support: Available in dashboard
