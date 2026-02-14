#!/bin/bash

# ============================================
# DEPLOYMENT SCRIPT FOR SHOPPING APPLICATION
# ============================================

echo "🚀 Starting deployment process..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "📝 Please copy .env.example to .env and fill in your values"
    exit 1
fi

# Load environment variables
source .env

# Check required variables
echo "🔍 Checking required environment variables..."

REQUIRED_VARS=("RAZORPAY_KEY" "RAZORPAY_SECRET" "ADMIN_PASSWORD")
MISSING_VARS=()

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo "❌ Error: Missing required environment variables:"
    printf '   - %s\n' "${MISSING_VARS[@]}"
    exit 1
fi

# Check if using LIVE Razorpay keys
if [[ "$RAZORPAY_KEY" == *"rzp_test_"* ]]; then
    echo "⚠️  WARNING: You're using TEST Razorpay keys!"
    echo "   For production, use LIVE keys (rzp_live_...)"
    read -p "   Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check password strength
if [ ${#ADMIN_PASSWORD} -lt 12 ]; then
    echo "⚠️  WARNING: Admin password is less than 12 characters!"
    echo "   For security, use a stronger password"
    read -p "   Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "✅ All checks passed!"

# Build the application
echo "📦 Building application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"

# Export environment variables for Spring Boot
export SPRING_PROFILES_ACTIVE=prod
export RAZORPAY_KEY
export RAZORPAY_SECRET
export ADMIN_USERNAME
export ADMIN_PASSWORD
export GOOGLE_CLIENT_ID
export GOOGLE_CLIENT_SECRET
export GITHUB_CLIENT_ID
export GITHUB_CLIENT_SECRET

echo "🚀 Starting application with production profile..."
echo "📍 Application will run on port: ${PORT:-8080}"

java -jar -Dspring.profiles.active=prod target/shopping-0.0.1-SNAPSHOT.jar
