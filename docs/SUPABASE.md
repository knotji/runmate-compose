# Supabase foundation

The Android client checks the Auth health endpoint, supports existing email/password accounts, restores an encrypted session, refreshes an expired token, and reads a minimal owner profile. It does not create accounts, mutate data, or upload health records.

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
- Profile access is a filtered read of `profiles` where `id` equals the authenticated user ID.
- No table write or production migration exists.
- A two-account RLS device test remains required before broadening table access.
