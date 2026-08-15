# Supabase foundation

The Android client currently performs one safe operation: it calls the project REST root to verify that the configured public project endpoint is reachable. It does not authenticate, query a table, mutate data, or upload health records.

## Local configuration

Add these ignored Gradle properties to `local.properties`:

```properties
wholeMateSupabaseUrl=https://PROJECT_REF.supabase.co
wholeMateSupabasePublishableKey=sb_publishable_REPLACE_ME
```

CI may instead provide `WHOLEMATE_SUPABASE_URL` and `WHOLEMATE_SUPABASE_PUBLISHABLE_KEY` environment variables. Use only a publishable key (or legacy anon key during migration). Never place a secret or service-role key in a mobile build.

## Safety boundary

- No credential value is logged or rendered.
- No account session exists yet.
- No table access or production write exists yet.
- All future table access requires authenticated Row Level Security tests with isolated accounts.
