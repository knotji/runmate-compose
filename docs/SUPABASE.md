# Supabase foundation

The Android client uses a dedicated auth gate with Google OAuth PKCE, restores an encrypted session, refreshes an expired token, and reads a minimal owner profile. Email/password repository support remains diagnostic-only. It does not create accounts, mutate data, or upload health records.

## Local configuration

Add these ignored Gradle properties to `local.properties`:

```properties
wholeMateSupabaseUrl=https://PROJECT_REF.supabase.co
wholeMateSupabasePublishableKey=sb_publishable_REPLACE_ME
```

CI may instead provide `WHOLEMATE_SUPABASE_URL` and `WHOLEMATE_SUPABASE_PUBLISHABLE_KEY` environment variables. Use only a publishable key (or legacy anon key during migration). Never place a secret or service-role key in a mobile build.

## Safety boundary

- No credential value is logged or rendered.
- Access and refresh tokens are encrypted with a non-exportable Android Keystore AES-GCM key.
- The temporary PKCE verifier is encrypted in the same vault and consumed once on callback.
- The registered callback is `com.wholemate.app://auth/callback`.
- Profile access is a filtered read of `profiles` where `id` equals the authenticated user ID.
- No table write or production migration exists.
- A two-account RLS device test remains required before broadening table access.
