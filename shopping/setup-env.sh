#!/bin/bash

# ============================================
# ENVIRONMENT VARIABLES SETUP SCRIPT
# ============================================
# This script helps you set environment variables
# Run: bash setup-env.sh

echo "🔧 Setting up environment variables for deployment..."
echo ""

# Check if .env file exists
if [ -f .env ]; then
    echo "⚠️  .env file already exists!"
    read -p "   Overwrite? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Cancelled"
        exit 1
    fi
fi

echo "📝 Please provide the following information:"
echo ""

# Razorpay Keys
read -p "Razorpay Key (rzp_live_...): " RAZORPAY_KEY
read -sp "Razorpay Secret: " RAZORPAY_SECRET
echo ""

# Admin Password
read -sp "Admin Password (min 12 chars): " ADMIN_PASSWORD
echo ""

# OAuth (Optional)
read -p "Google Client ID (optional, press Enter to skip): " GOOGLE_CLIENT_ID
read -sp "Google Client Secret (optional, press Enter to skip): " GOOGLE_CLIENT_SECRET
echo ""
read -p "GitHub Client ID (default: Ov23liCogGkwyc6V02jL): " GITHUB_CLIENT_ID
GITHUB_CLIENT_ID=${GITHUB_CLIENT_ID:-Ov23liCogGkwyc6V02jL}
read -sp "GitHub Client Secret (optional, press Enter to skip): " GITHUB_CLIENT_SECRET
echo ""

# Create .env file
cat > .env << EOF
# Environment Variables for Shopping Application
# Generated on $(date)

# Razorpay Configuration
RAZORPAY_KEY=$RAZORPAY_KEY
RAZORPAY_SECRET=$RAZORPAY_SECRET

# Admin Credentials
ADMIN_USERNAME=admin
ADMIN_PASSWORD=$ADMIN_PASSWORD

# OAuth Configuration
GOOGLE_CLIENT_ID=$GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET=$GOOGLE_CLIENT_SECRET
GITHUB_CLIENT_ID=$GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET=$GITHUB_CLIENT_SECRET

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
EOF

echo ""
echo "✅ .env file created successfully!"
echo "📋 Review the file: cat .env"
echo ""
echo "⚠️  IMPORTANT:"
echo "   - DO NOT commit .env to git!"
echo "   - Add .env to .gitignore"
echo "   - Set these variables in your hosting platform"
