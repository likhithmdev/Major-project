# Firebase Setup Guide for Smart Ambulance Project

## Overview
Your Smart Ambulance project uses Firebase for:
- **Realtime Database** - Live ambulance tracking, junction status, events
- **Authentication** - User login for different roles (driver, police, hospital, admin)
- **Cloud Functions** (optional) - Server-side logic if needed

## Current Status
✅ **Firebase dependencies** are already configured in `mobile_app/app/build.gradle.kts`
❌ **`google-services.json`** is missing - this is the critical file needed

## Firebase Setup Steps

### 1. Create/Access Firebase Project

**Option A: Use Existing Project**
- Project ID: `smart-ambulance-36f9d`
- Database URL: `https://smart-ambulance-36f9d-default-rtdb.firebaseio.com`
- Auth domain: `smart-ambulance-36f9d.firebaseapp.com`

**Option B: Create New Project**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name (e.g., "Smart Ambulance")
4. Follow the setup wizard

### 2. Add Android App to Firebase

1. In Firebase Console, click the **Android icon** (Add app)
2. **Package name**: `com.smartambulance.driver`
3. **App nickname**: `Smart Ambulance Driver App`
4. **Debug signing certificate SHA-1**: (optional, but recommended for production)

### 3. Download `google-services.json`

1. After adding the Android app, Firebase will prompt you to download `google-services.json`
2. Download the file
3. **Place it in**: `mobile_app/app/google-services.json`

This is the **most critical step** - without this file, the app cannot connect to Firebase.

### 4. Configure Firebase Realtime Database

#### Enable Realtime Database
1. In Firebase Console, go to **Build** → **Realtime Database**
2. Click **Create Database**
3. Choose a location (select closest to your users)
4. **Security Rules**: Start in **Test Mode** for development:
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

#### Set Up Database Structure
The app expects this structure:

```
smart-ambulance-36f9d-default-rtdb.firebaseio.com/
├── users/
│   ├── driver_001/
│   │   ├── name: "John Driver"
│   │   ├── role: "ambulance_driver"
│   │   └── ambulanceId: "AMB001"
│   ├── police_001/
│   │   ├── name: "Police Officer"
│   │   ├── role: "police"
│   │   └── junctionId: "JNC001"
│   ├── hospital_001/
│   │   ├── name: "City Hospital"
│   │   ├── role: "hospital"
│   │   └── hospitalId: "HOSP001"
│   └── admin_001/
│       ├── name: "System Admin"
│       ├── role: "admin"
│       └── permissions: "full"
├── ambulances/
│   └── AMB001/
│       ├── driverId: "driver_001"
│       ├── currentLocation: { lat: 12.9716, lng: 77.5946 }
│       ├── status: "active"
│       └── lastUpdate: 1234567890
├── emergencyTrips/
│   └── TRIP001/
│       ├── ambulanceId: "AMB001"
│       ├── driverId: "driver_001"
│       ├── hospitalId: "HOSP001"
│       ├── severity: "Very Emergency"
│       ├── status: "active"
│       ├── startTime: 1234567890
│       └── route: [...]
├── junctions/
│   ├── JNC001/
│   │   ├── name: "Main Road Junction"
│   │   ├── location: { lat: 12.9716, lng: 77.5946 }
│   │   ├── status: "normal"
│   │   ├── currentSignal: "green"
│   │   └── lastPreemption: 1234567890
│   ├── JNC002/
│   │   └── ...
│   └── JNC003/
│       └── ...
├── junctionEvents/
│   └── JNC001/
│       ├── event_001/
│       │   ├── type: "GPS_PREEMPT"
│       │   ├── ambulanceId: "AMB001"
│       │   ├── timestamp: 1234567890
│       │   └── details: {...}
│       └── event_002/
│           └── ...
├── loraTelemetry/
│   └── AMB001/
│       ├── lat: 12.9716
│       ├── lng: 77.5946
│       ├── heading: 45.0
│       ├── speed: 60.0
│       └── timestamp: 1234567890
├── hospitals/
│   ├── HOSP001/
│   │   ├── name: "City Hospital"
│   │   ├── location: { lat: 12.9750, lng: 77.5950 }
│   │   ├── phone: "+911234567890"
│   │   ├── emergencyWard: true
│   │   └── bedCapacity: 50
│   └── HOSP002/
│       └── ...
├── hospitalAlerts/
│   └── HOSP001/
│       ├── alert_001/
│       │   ├── tripId: "TRIP001"
│       │   ├── ambulanceId: "AMB001"
│       │   ├── eta: "5 min"
│       │   ├── severity: "Very Emergency"
│       │   └── timestamp: 1234567890
│       └── ...
├── policeAlerts/
│   └── JNC001/
│       ├── alert_001/
│       │   ├── tripId: "TRIP001"
│       │   ├── ambulanceId: "AMB001"
│       │   ├── junctionId: "JNC001"
│       │   └── timestamp: 1234567890
│       └── ...
└── rfidTags/
    ├── RFID_TAG_001/
    │   ├── tagId: "RFID_TAG_001"
    │   ├── assignedTo: "AMB001"
    │   └── lastUsed: 1234567890
    └── ...
```

### 5. Configure Firebase Authentication

#### Enable Authentication
1. In Firebase Console, go to **Build** → **Authentication**
2. Click **Get Started**
3. Enable **Email/Password** sign-in method
4. (Optional) Enable **Anonymous** auth for testing

#### Create Test Users
For development, create these users in Firebase Console → Authentication → Users:

| Email | Password | Role | Purpose |
|-------|----------|------|---------|
| driver@smartambulance.com | driver123 | Driver | Ambulance driver app |
| police@smartambulance.com | police123 | Police | Junction monitoring |
| hospital@smartambulance.com | hospital123 | Hospital | Hospital notifications |
| admin@smartambulance.com | admin123 | Admin | System administration |

**Note**: The app currently uses custom user IDs (driver_001, police_001, etc.) with a PIN system, not email/password. You may need to adapt the authentication flow.

### 6. Update Dashboard Firebase Configuration

The dashboard also needs Firebase configuration. Update `dashboard/src/integrations/firebaseClient.js`:

```javascript
const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "smart-ambulance-36f9d.firebaseapp.com",
  databaseURL: "https://smart-ambulance-36f9d-default-rtdb.firebaseio.com",
  projectId: "smart-ambulance-36f9d",
  storageBucket: "smart-ambulance-36f9d.appspot.com",
  messagingSenderId: "YOUR_SENDER_ID",
  appId: "YOUR_APP_ID"
};
```

You can find these values in Firebase Console → Project Settings → General → Your apps.

### 7. Security Rules (Production)

For production, update Realtime Database rules:

```json
{
  "rules": {
    "users": {
      ".read": "auth != null",
      "$userId": {
        ".write": "auth.uid == $userId || root.child('users').child(auth.uid).child('role').val() == 'admin'"
      }
    },
    "ambulances": {
      ".read": "auth != null",
      "$ambulanceId": {
        ".write": "auth != null && root.child('users').child(auth.uid).child('ambulanceId').val() == $ambulanceId"
      }
    },
    "emergencyTrips": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "junctions": {
      ".read": "auth != null",
      ".write": "auth != null && root.child('users').child(auth.uid).child('role').val() == 'admin'"
    },
    "junctionEvents": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "loraTelemetry": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "hospitals": {
      ".read": "auth != null",
      ".write": "auth != null && root.child('users').child(auth.uid).child('role').val() == 'admin'"
    },
    "hospitalAlerts": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "policeAlerts": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

### 8. Initialize Demo Data

The app has a `DemoRepository` that loads demo data. You can either:
- Let the app create demo data automatically (currently implemented)
- Manually create the database structure in Firebase Console
- Use the "Load demo accounts" button in the app

### 9. Test the Connection

After setup, test the connection:

1. **Build and run the mobile app**
2. **Try to login** with demo credentials (driver_001 / 1111)
3. **Check Firebase Console** → Realtime Database → Data
4. **Verify data appears** when you activate emergency mode

### 10. Common Issues & Solutions

#### Issue: "google-services.json not found"
**Solution**: Ensure `google-services.json` is in `mobile_app/app/` directory

#### Issue: "Authentication failed"
**Solution**: 
- Check if Authentication is enabled in Firebase Console
- Verify user exists in Authentication tab
- Check if email/password or custom auth is properly configured

#### Issue: "Database permission denied"
**Solution**: 
- Check Realtime Database rules
- Ensure rules allow read/write for authenticated users
- For development, use test mode rules

#### Issue: "No data showing in dashboard"
**Solution**:
- Verify Firebase configuration in `firebaseClient.js`
- Check browser console for errors
- Ensure API key and database URL are correct

## Firebase Free Tier Limits

- **Realtime Database**: 1GB stored, 10GB/month downloaded, 100GB/month uploaded
- **Authentication**: 10,000 verifications/month
- **Hosting**: 10GB/month, 10GB/month bandwidth

For a proof-of-concept, the free tier should be sufficient.

## Summary

**Critical Steps:**
1. ✅ Create/access Firebase project
2. ✅ Add Android app to Firebase
3. ✅ **Download `google-services.json`** and place in `mobile_app/app/`
4. ✅ Enable Realtime Database with test mode rules
5. ✅ Enable Authentication
6. ✅ Update dashboard Firebase configuration
7. ✅ Test the connection

**Most Important File**: `mobile_app/app/google-services.json` - without this, the app cannot connect to Firebase at all.

Once these steps are complete, your Smart Ambulance system will have full Firebase integration for real-time tracking, authentication, and data persistence.