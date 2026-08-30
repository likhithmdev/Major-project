# Firebase Database Rules - Demo Mode (For Testing)

## Current Issue
The permission denied error occurs because the production security rules require proper role-based access, but the demo data might not be set up correctly or the rules are too restrictive for testing.

## Solution: Use Demo Rules First

For testing and development, use these simpler rules that allow authenticated users to access the data they need:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

## How to Apply Demo Rules

### Step 1: Go to Firebase Console
1. Open [Firebase Console](https://console.firebase.google.com/)
2. Select project: `smart-ambulance-36f9d`
3. Go to **Build** → **Realtime Database**
4. Click the **Rules** tab

### Step 2: Apply Demo Rules
1. Delete the current rules
2. Copy the demo rules above
3. Paste them into the rules editor
4. Click **Publish**

### Step 3: Enable Anonymous Authentication

The app now uses Firebase Authentication sign-in before reading the database. You must enable Anonymous authentication:

1. In the left sidebar, click **Build**
2. Click **Authentication**
3. Click **Get Started** (if not already enabled)
4. Click the **Sign-in method** tab
5. Enable **Anonymous** sign-in method
6. Click **Save**

### Step 4: Seed Demo Data
After applying demo rules, make sure to seed the demo data:

1. Open the Smart Ambulance app
2. Click **"Load demo accounts"** button on the login screen
3. Wait for the message: "Firebase demo data ready"
4. Now try logging in with: `driver_001` / `1111`

## Why This Happens

The "permission denied" error occurs because:

1. **No Firebase Authentication**: The app was not signing in to Firebase Authentication before reading the database. The rules require `auth != null`, but the app had no authenticated user.

2. **Database Rules Too Restrictive**: The production rules require:
   - Users to have specific roles set correctly
   - Proper data structure in the database
   - Authentication to be fully configured

3. **Demo Data Not Seeded**: The demo users might not exist in the database yet, so even with proper rules, the login fails

4. **Data Structure Mismatch**: The rules expect specific data structure that might not match the current database state

## What Was Fixed

The app has been updated to:
- Sign in to Firebase Authentication anonymously on app startup
- Ensure Firebase Auth is signed in before reading the database
- This satisfies the `auth != null` requirement in the demo rules

## Testing After Demo Rules

Once demo rules are applied and demo data is seeded:

1. **Test Login**: Try logging in with `driver_11` / `1111`
2. **Test Roles**: Try logging in with different roles:
   - `police_001` / `2222`
   - `hospital_001` / `3333`
   - `admin_001` / `0000`
3. **Test Emergency**: Activate emergency mode and verify data appears in Firebase

## Transition to Production Rules

Once everything works with demo rules:

1. **Verify Data Structure**: Ensure all users have correct roles
2. **Test Role-Based Access**: Verify users can only access their data
3. **Apply Production Rules**: Use the production rules from `docs/FIREBASE_SECURITY_RULES.md`
4. **Test Thoroughly**: Test all roles and access patterns
5. **Monitor**: Watch for permission errors in Firebase Console

## Alternative: Hybrid Rules

If you want something between demo and production, use these hybrid rules:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "users": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "admin": {
      ".read": "root.child('users').child(auth.uid).child('role').val() == 'admin'",
      ".write": "root.child('users').child(auth.uid).child('role').val() == 'admin'"
    }
  }
}
```

This allows:
- ✅ All authenticated users to read/write most data
- ✅ Only admins can access the admin section
- ✅ Less restrictive than full production rules
- ✅ More secure than pure demo rules

## Quick Fix Steps

1. **Immediate Fix**: Apply demo rules (read/write for all authenticated users)
2. **Seed Data**: Click "Load demo accounts" in the app
3. **Test Login**: Try driver_001 / 1111
4. **Verify**: Check Firebase Console → Realtime Database → Data to see demo data

## If Still Having Issues

If you still get permission denied after applying demo rules:

1. **Check Authentication**: Verify Firebase Authentication is enabled
2. **Check API Key**: Verify google-services.json is correct
3. **Check Network**: Ensure you have internet connectivity
4. **Check Firebase Console**: Look for any error messages in the console
5. **Check User Data**: Verify demo users exist in Authentication tab

## Monitoring

After fixing the issue, monitor:

1. **Firebase Console** → Realtime Database → Usage (for access patterns)
2. **Firebase Console** → Authentication → Users (verify users exist)
3. **App Logs**: Check for Firebase-related error messages
4. **Network**: Monitor for connectivity issues

## Summary

**Current Status**: 🔴 Permission denied error

**Immediate Fix**: Apply demo rules (read/write for all authenticated users)

**Next Steps**:
1. Apply demo rules in Firebase Console
2. Seed demo data in the app
3. Test login with driver_001 / 1111
4. Transition to production rules once everything works