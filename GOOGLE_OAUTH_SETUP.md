# Google OAuth 2.0 Setup for D-Time

This guide explains how to set up Google OAuth 2.0 authentication for the D-Time application.

## Overview

D-Time supports Google OAuth 2.0 as an alternative login method. When enabled, users can log in with their Google accounts, but **they must already exist in the D-Time database**. This is a hybrid approach where Google OAuth is used for authentication, but user management remains within D-Time.

## Prerequisites

1. A Google Cloud Project
2. A user account already created in D-Time with the same email address as the Google account

## Google Cloud Console Setup

### Step 1: Create a Google Cloud Project (if needed)

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API (required for OAuth 2.0)

### Step 2: Create OAuth 2.0 Credentials

1. In the Google Cloud Console, navigate to **APIs & Services > Credentials**
2. Click **"Create Credentials"** > **"OAuth 2.0 Client IDs"**
3. If prompted, configure the OAuth consent screen first:
   - Choose **External** user type (unless you have Google Workspace)
   - Fill in the required fields:
     - App name: `D-Time`
     - User support email: Your email
     - Developer contact information: Your email
   - Add scopes: `email`, `profile` (these are non-sensitive scopes)
   - Save and continue

4. Create the OAuth 2.0 Client ID:
   - Application type: **Web application**
   - Name: `D-Time Application`
   - Authorized redirect URIs:
     - For development: `http://localhost:8080/login/oauth2/code/google`
     - For production: `https://your-domain.com/login/oauth2/code/google`
   
5. Save and note down:
   - **Client ID** (looks like: `123456789-abcdefg.apps.googleusercontent.com`)
   - **Client Secret** (looks like: `GOCSPX-abcdefghijklmnop`)

## D-Time Configuration

### Step 1: Environment Variables

Add the following to your `.env` file:

```bash
# Enable Google OAuth
OAUTH_GOOGLE_ENABLED=true

# Google OAuth Credentials
OAUTH_GOOGLE_CLIENT_ID=your-client-id.googleusercontent.com
OAUTH_GOOGLE_CLIENT_SECRET=your-client-secret
```

### Step 2: User Setup

**Important**: Users must exist in D-Time before they can use Google OAuth.

1. Create users in D-Time with their Google email addresses
2. Ensure user status is **ACTIVE**
3. The user's email in D-Time must exactly match their Google account email

### Step 3: Deploy/Restart Application

After configuring the environment variables:

```bash
# For Docker deployment
./deploy.sh --env production

# For development
./deploy.sh --env development
```

## How It Works

1. **User clicks "Sign in with Google"** on the login page
2. **User is redirected to Google** for authentication
3. **Google redirects back** with user information
4. **D-Time validates** that the user exists in the database
5. **If user exists and is active**, login succeeds
6. **If user doesn't exist**, login fails with error message

## Security Features

- **Database validation**: Only existing D-Time users can login via Google
- **Active status check**: Inactive users cannot login even with valid Google account
- **Email matching**: User email must exactly match Google account email
- **Error handling**: Clear error messages for failed OAuth attempts

## Troubleshooting

### "User not found" Error

**Problem**: User gets error "User with email X not found in the system"

**Solution**: 
1. Ensure the user exists in D-Time database
2. Verify the email addresses match exactly
3. Check that the user status is ACTIVE

### "Google login failed" Error

**Problem**: Generic OAuth error

**Possible causes**:
- Invalid Google OAuth credentials
- Incorrect redirect URI configuration
- Google consent screen not properly configured
- Network/connectivity issues

**Solutions**:
1. Verify `OAUTH_GOOGLE_CLIENT_ID` and `OAUTH_GOOGLE_CLIENT_SECRET` are correct
2. Check redirect URI matches exactly (including HTTP/HTTPS)
3. Ensure Google consent screen is published (not in testing mode)
4. Check server logs for detailed error messages

### Google OAuth Button Not Showing

**Problem**: Only username/password login is visible

**Possible causes**:
- `OAUTH_GOOGLE_ENABLED` is not set to `true`
- Frontend cannot reach `/api/auth/google/status` endpoint
- Backend is not running

**Solutions**:
1. Set `OAUTH_GOOGLE_ENABLED=true` in environment
2. Restart the application
3. Check browser developer console for network errors
4. Verify backend is responding to API calls

## Testing

1. **Create a test user** in D-Time with your Google email
2. **Enable Google OAuth** in environment configuration
3. **Restart the application**
4. **Navigate to login page** - should see "Sign in with Google" button
5. **Click Google login** - should redirect to Google
6. **Complete Google authentication** - should redirect back and login successfully

## Production Considerations

1. **HTTPS Required**: Google OAuth requires HTTPS in production
2. **Domain Verification**: May need to verify domain ownership in Google Console
3. **Rate Limits**: Google has rate limits on OAuth requests
4. **Monitoring**: Monitor OAuth login success/failure rates
5. **User Communication**: Inform users they need existing D-Time accounts

## Configuration Summary

The complete configuration involves:

- **Google Cloud Console**: OAuth 2.0 Client ID with correct redirect URIs
- **Environment Variables**: Enable OAuth and set credentials
- **User Database**: Ensure users exist with matching email addresses
- **Application Deployment**: Restart with new configuration

When properly configured, users will see both login options and can choose their preferred method.