# Deploy Payment Backend to Render.com - Step-by-Step Guide

This guide will help you deploy your FixMate payment backend to Render.com for free.

## 🎯 What is Render.com?

Render is a modern cloud platform that automatically deploys your code from GitHub. It's perfect for hosting your payment backend with:
- ✅ Free tier available (sleeps after 15 min of inactivity)
- ✅ Automatic HTTPS
- ✅ Automatic deployments from GitHub
- ✅ Easy environment variable management

## 📋 Prerequisites

Before starting, make sure you have:
1. ✅ A GitHub account
2. ✅ Stripe account with test API keys
3. ✅ Firebase project with service account JSON
4. ✅ Payment backend code ready to deploy

---

## 🚀 Step 1: Prepare Your Backend Code

### 1.1 Create Backend Project Structure

Create a new folder for your backend:
```
fixmate-payment-backend/
├── server.js           # Main server file
├── package.json        # Dependencies
├── .env.example        # Example environment variables
├── .gitignore          # Git ignore file
└── README.md           # Documentation
```

### 1.2 Create `package.json`

```json
{
  "name": "fixmate-payment-backend",
  "version": "1.0.0",
  "description": "Payment backend for FixMate app using Stripe",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "engines": {
    "node": ">=18.0.0"
  },
  "dependencies": {
    "express": "^4.18.2",
    "stripe": "^14.10.0",
    "firebase-admin": "^12.0.0",
    "dotenv": "^16.3.1",
    "cors": "^2.8.5",
    "helmet": "^7.1.0",
    "express-rate-limit": "^7.1.5"
  },
  "devDependencies": {
    "nodemon": "^3.0.2"
  }
}
```

### 1.3 Create `server.js`

```javascript
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);
const admin = require('firebase-admin');

const app = express();
const PORT = process.env.PORT || 3000;

// Initialize Firebase Admin
if (!admin.apps.length) {
  // For Render.com, use environment variable for service account
  const serviceAccount = process.env.FIREBASE_SERVICE_ACCOUNT 
    ? JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)
    : require('./firebase-service-account.json');
  
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

const db = admin.firestore();

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100 // limit each IP to 100 requests per windowMs
});
app.use('/api/', limiter);

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'FixMate Payment Backend is running',
    timestamp: new Date().toISOString()
  });
});

// Create Payment Intent
app.post('/api/payments/create-intent', async (req, res) => {
  try {
    const { bookingId, amount, customerId, providerId } = req.body;

    console.log('Creating payment intent:', { bookingId, amount, customerId, providerId });

    // Validate input
    if (!bookingId || !amount || !customerId || !providerId) {
      return res.status(400).json({ 
        error: 'Missing required fields: bookingId, amount, customerId, providerId' 
      });
    }

    // Create Stripe payment intent
    const paymentIntent = await stripe.paymentIntents.create({
      amount: Math.round(amount), // Amount in cents
      currency: 'lkr',
      metadata: {
        bookingId,
        customerId,
        providerId
      },
      automatic_payment_methods: {
        enabled: true,
      },
    });

    console.log('Payment intent created:', paymentIntent.id);

    res.json({
      clientSecret: paymentIntent.client_secret,
      paymentIntentId: paymentIntent.id,
      publishableKey: process.env.STRIPE_PUBLISHABLE_KEY
    });
  } catch (error) {
    console.error('Error creating payment intent:', error);
    res.status(500).json({ error: error.message });
  }
});

// Confirm Payment
app.post('/api/payments/confirm', async (req, res) => {
  try {
    const { paymentIntentId, bookingId } = req.body;

    console.log('Confirming payment:', { paymentIntentId, bookingId });

    // Retrieve payment intent from Stripe
    const paymentIntent = await stripe.paymentIntents.retrieve(paymentIntentId);

    if (paymentIntent.status === 'succeeded') {
      // Update booking status in Firestore
      await db.collection('bookings').doc(bookingId).update({
        status: 'COMPLETED',
        'pricing.paymentStatus': 'COMPLETED',
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Create payment record
      await db.collection('payments').add({
        bookingId,
        paymentIntentId,
        amount: paymentIntent.amount / 100,
        currency: paymentIntent.currency,
        status: 'COMPLETED',
        method: 'CARD',
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });

      console.log('Payment confirmed and booking updated');

      res.json({
        success: true,
        message: 'Payment confirmed successfully',
        paymentStatus: 'COMPLETED',
        bookingStatus: 'COMPLETED'
      });
    } else {
      res.status(400).json({
        success: false,
        message: `Payment status is ${paymentIntent.status}`
      });
    }
  } catch (error) {
    console.error('Error confirming payment:', error);
    res.status(500).json({ error: error.message });
  }
});

// Process Cash Payment
app.post('/api/payments/cash', async (req, res) => {
  try {
    const { bookingId, amount, customerId, providerId } = req.body;

    console.log('Processing cash payment:', { bookingId, amount, customerId, providerId });

    // Update booking status
    await db.collection('bookings').doc(bookingId).update({
      status: 'COMPLETED',
      'pricing.paymentStatus': 'COMPLETED',
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // Create payment record
    const paymentRef = await db.collection('payments').add({
      bookingId,
      amount,
      currency: 'LKR',
      status: 'COMPLETED',
      method: 'CASH',
      customerId,
      providerId,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    console.log('Cash payment processed:', paymentRef.id);

    res.json({
      success: true,
      message: 'Cash payment processed successfully',
      paymentId: paymentRef.id,
      bookingStatus: 'COMPLETED'
    });
  } catch (error) {
    console.error('Error processing cash payment:', error);
    res.status(500).json({ error: error.message });
  }
});

// Start server
app.listen(PORT, () => {
  console.log(`✅ FixMate Payment Backend running on port ${PORT}`);
  console.log(`🌐 Health check: http://localhost:${PORT}/health`);
});
```

### 1.4 Create `.env.example`

```env
# Stripe API Keys (Get from https://dashboard.stripe.com/apikeys)
STRIPE_SECRET_KEY=sk_test_your_secret_key_here
STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable_key_here

# Server Port
PORT=3000

# Firebase (Paste entire service account JSON as one line)
FIREBASE_SERVICE_ACCOUNT={"type":"service_account","project_id":"your-project-id",...}
```

### 1.5 Create `.gitignore`

```
node_modules/
.env
firebase-service-account.json
*.log
.DS_Store
```

---

## 📤 Step 2: Push Code to GitHub

### 2.1 Create New GitHub Repository

1. Go to https://github.com/new
2. Repository name: `fixmate-payment-backend`
3. Description: "Payment backend for FixMate app"
4. Choose **Public** or **Private**
5. Click **Create repository**

### 2.2 Push Your Code

```bash
cd fixmate-payment-backend
git init
git add .
git commit -m "Initial commit: Payment backend"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/fixmate-payment-backend.git
git push -u origin main
```

---

## 🌐 Step 3: Deploy to Render.com

### 3.1 Create Render Account

1. Go to https://render.com
2. Click **Get Started**
3. Sign up with GitHub (recommended) or email
4. Verify your email

### 3.2 Create New Web Service

1. Click **Dashboard** → **New +** → **Web Service**
2. Click **Connect account** to connect your GitHub
3. Select **fixmate-payment-backend** repository
4. Click **Connect**

### 3.3 Configure Web Service

Fill in the following details:

| Field | Value |
|-------|-------|
| **Name** | `fixmate-payment-backend` |
| **Region** | Choose closest to you (e.g., Singapore) |
| **Branch** | `main` |
| **Runtime** | `Node` |
| **Build Command** | `npm install` |
| **Start Command** | `npm start` |
| **Instance Type** | `Free` |

### 3.4 Add Environment Variables

Click **Advanced** → **Add Environment Variable** and add these:

1. **STRIPE_SECRET_KEY**
   - Value: `sk_test_...` (from Stripe Dashboard)
   
2. **STRIPE_PUBLISHABLE_KEY**
   - Value: `pk_test_...` (from Stripe Dashboard)

3. **FIREBASE_SERVICE_ACCOUNT**
   - Get Firebase service account JSON:
     1. Firebase Console → Project Settings → Service Accounts
     2. Click "Generate new private key"
     3. Open the downloaded JSON file
     4. Copy the ENTIRE JSON (all in one line)
     5. Paste as value
   - Example: `{"type":"service_account","project_id":"your-project",...}`

### 3.5 Deploy!

1. Click **Create Web Service**
2. Wait 2-5 minutes for deployment
3. Watch the build logs in real-time

---

## ✅ Step 4: Test Your Deployment

### 4.1 Get Your Backend URL

After deployment completes, you'll see:
```
Your service is live at https://fixmate-payment-backend.onrender.com
```

### 4.2 Test Health Endpoint

Open in browser or use curl:
```bash
curl https://fixmate-payment-backend.onrender.com/health
```

Should return:
```json
{
  "status": "OK",
  "message": "FixMate Payment Backend is running",
  "timestamp": "2025-12-12T10:30:00.000Z"
}
```

### 4.3 Test Payment Intent (Optional)

Use Postman or curl:
```bash
curl -X POST https://fixmate-payment-backend.onrender.com/api/payments/create-intent \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "test123",
    "amount": 20000,
    "customerId": "cust123",
    "providerId": "prov123"
  }'
```

---

## 🔄 Step 5: Update Android App

### 5.1 Update NetworkModule.kt

```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://fixmate-payment-backend.onrender.com/")  // ⬅️ Your Render URL
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

### 5.2 Rebuild and Test

```bash
./gradlew clean assembleDebug installDebug
```

---

## ⚠️ Important Notes

### Free Tier Limitations

1. **Cold Starts**: Backend sleeps after 15 minutes of inactivity
   - First request after sleep takes 30-60 seconds
   - Subsequent requests are instant
   - Consider upgrading to paid plan ($7/month) for always-on

2. **Build Minutes**: 500 free build minutes/month
   - Each deployment counts
   - Monitor in Render Dashboard

3. **Bandwidth**: 100GB free/month
   - More than enough for testing

### Production Recommendations

For production (real users):
1. Upgrade to **Starter Plan** ($7/month) - No cold starts
2. Use **live Stripe keys** instead of test keys
3. Enable **Auto-Deploy** from main branch
4. Set up **health checks** and **alerts**

---

## 🐛 Troubleshooting

### Issue: "Application Error" on Render

**Solution:**
- Check build logs in Render Dashboard
- Ensure `package.json` has `"start": "node server.js"`
- Verify all environment variables are set

### Issue: "FIREBASE_SERVICE_ACCOUNT error"

**Solution:**
- Ensure JSON is copied as **one continuous line**
- No line breaks or extra spaces
- Check JSON is valid at https://jsonlint.com

### Issue: Android app shows "Cannot connect"

**Solution:**
1. Verify backend URL in NetworkModule.kt
2. Test health endpoint in browser
3. Check backend logs in Render Dashboard
4. Wait 60 seconds if backend was sleeping

### Issue: Payment fails with "Invalid API key"

**Solution:**
- Verify Stripe keys are correct
- Use **test keys** (start with `sk_test_` and `pk_test_`)
- Check keys are copied completely (no truncation)

---

## 📊 Monitor Your Backend

### View Logs
1. Render Dashboard → Your Service
2. Click **Logs** tab
3. Watch real-time requests and errors

### Check Metrics
1. Click **Metrics** tab
2. Monitor:
   - Response times
   - Memory usage
   - CPU usage
   - Request count

---

## 🎉 Success Checklist

- ✅ GitHub repository created
- ✅ Code pushed to GitHub
- ✅ Render.com account created
- ✅ Web Service deployed
- ✅ Environment variables configured
- ✅ Health endpoint returns OK
- ✅ Android app NetworkModule updated
- ✅ Test payment works in app

---

## 🚀 Next Steps

1. **Test Payments**: 
   - Use Stripe test card: `4242 4242 4242 4242`
   - Any CVC, future expiry date
   
2. **Monitor Usage**:
   - Check Render Dashboard regularly
   - Review payment logs in Stripe Dashboard

3. **Go Live** (when ready):
   - Replace test Stripe keys with live keys
   - Upgrade Render plan
   - Update Android app for production

---

## 📞 Support Resources

- **Render Docs**: https://render.com/docs
- **Stripe Docs**: https://stripe.com/docs/api
- **Firebase Admin**: https://firebase.google.com/docs/admin/setup

---

**🎊 Congratulations! Your payment backend is now live on Render.com!**

The backend URL to use in your Android app is:
```
https://fixmate-payment-backend.onrender.com/
```
