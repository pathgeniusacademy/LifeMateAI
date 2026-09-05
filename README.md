# LifeMate AI — Premium V3

LifeMate AI is a native Android daily-life organizer built with Kotlin + Jetpack Compose. V3 focuses on a polished, easy UI and a real secure AI connection without placing an API key inside the APK.

## What is improved in V3
- Premium home dashboard with larger, consistent tap targets
- Home metric cards are now actionable
- **New task** opens task creation immediately
- **Quick note** opens note creation immediately
- Cleaner AI chat screen with connection state, retry, clear conversation and prompt chips
- AI receives useful app context such as open tasks, habits and recent notes
- Settings includes a **Test AI connection** button and clear connection status
- Optional build-time `AI_BACKEND_URL` support through a GitHub Actions secret
- Offline assistant still works when AI is unavailable
- Version updated to 3.0.0

## Core features
- Tasks with priority, due presets, completion and local reminders
- Daily planner grouped into morning / afternoon / evening
- Notes with search, edit, pin and delete
- Habits with daily completion and streaks
- Offline assistant commands
- Secure AI chat through your own HTTPS backend
- Light / dark / system themes
- Local-first storage
- GitHub Actions for APK and AAB

## Offline assistant examples
- `Add task buy groceries`
- `What do I have today?`
- `Plan my day`
- `Habit progress`
- `Remind me to call Sam tomorrow at 6 pm`
- `Kal 7 pm baje assignment submit karna yaad dilana`

## AI architecture
Android app → your HTTPS Cloudflare Worker → OpenAI Responses API.

The Android app never contains the OpenAI API key. The backend starter is in `backend/cloudflare-worker/`.

The backend defaults to `gpt-5.6-luna`, which is suitable for a cost-sensitive everyday assistant. You can change `OPENAI_MODEL` later.

## GitHub build
The included workflow uses JDK 17 and Gradle 8.13 and builds:

```bash
gradle :app:assembleDebug
gradle :app:bundleRelease
```

The project uses Android Gradle Plugin 8.11.1 and targets Android API 36.

## Optional: bake the backend URL into builds
After your backend is deployed, add a GitHub repository secret named:

`AI_BACKEND_URL`

with the Worker HTTPS URL. Future APK/AAB builds will start with that backend already configured. The user can still change it in Settings.

## Security
Never commit or place `OPENAI_API_KEY` inside the Android app, Gradle files, GitHub source, or APK. Keep it only as a server-side secret.

Before a public launch, add backend authentication, rate limiting and abuse controls so unknown users cannot consume your API budget.
