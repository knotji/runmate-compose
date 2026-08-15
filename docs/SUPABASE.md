# Supabase Foundation

WholeMate uses a shared auth/session repository contract with platform secure storage. The current Android host has a dedicated Google OAuth PKCE gate, restores an encrypted session, refreshes an expired token, and reads a minimal owner profile. Email/password repository support remains diagnostic-only. It does not create accounts, mutate data, or upload health records.

## Local configuration

Add these ignored Gradle properties to `local.properties`:

```properties
wholeMateSupabaseUrl=https://PROJECT_REF.supabase.co
wholeMateSupabasePublishableKey=sb_publishable_REPLACE_ME
```

CI may instead provide `WHOLEMATE_SUPABASE_URL` and `WHOLEMATE_SUPABASE_PUBLISHABLE_KEY` environment variables. Use only a publishable key (or legacy anon key during migration). Never place a secret or service-role key in a mobile build.

## Safety boundary

- No credential value is logged or rendered.
- `SessionVault` is the common interface; tokens never live in plain preferences.
- Android encrypts access/refresh tokens and the one-time PKCE verifier with a non-exportable Android Keystore AES-GCM key.
- A future iOS host uses Keychain behind the same contract; it does not imitate Android Keystore APIs.
- The registered callback is `com.wholemate.app://auth/callback`.
- Profile access is a filtered read of `profiles` where `id` equals the authenticated user ID.
- No table write or production migration exists.
- A two-account RLS device test remains required before broadening table access.
