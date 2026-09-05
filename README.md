# LifeMate AI — Android V1

A native Android daily-life assistant built with Kotlin + Jetpack Compose.

## V1 features
- Modern onboarding and dashboard
- Tasks with priority, due presets, completion and local reminders
- Daily planner grouped into morning / afternoon / evening
- Notes with search, edit, pin and delete
- Habits with daily completion and streaks
- Assistant chat UI
- Offline assistant commands that can create tasks/reminders without any AI API
- Optional secure AI backend connection
- Light / dark / system themes
- Local-first data storage
- GitHub Actions for APK, unsigned AAB, and signed Play Store AAB

## Offline assistant examples
- `Add task buy groceries`
- `What do I have today?`
- `Plan my day`
- `Habit progress`
- `Remind me to call Sam tomorrow at 6 pm`
- Hinglish example: `Kal 7 pm baje assignment submit karna yaad dilana`

## Architecture
- Android: Kotlin, Jetpack Compose, Navigation Compose
- Persistence: local SharedPreferences JSON (simple and private for V1)
- Reminders: WorkManager local notifications
- AI networking: Android calls only your HTTPS backend
- Backend starter: Cloudflare Worker -> OpenAI Responses API

## Security rule
Never put an OpenAI API key in Android source, `BuildConfig`, GitHub public files, JavaScript, or the APK. The provided backend reads the key from a server secret.

## Build
The included GitHub workflow installs JDK 17 and Gradle 8.13, then runs:

```bash
gradle :app:assembleDebug
gradle :app:bundleRelease
```

The project uses Android Gradle Plugin 8.11.1 and targets Android API 36.

## V1 limitations / next stage
- UI task creation currently offers quick due presets; precise times are already supported through assistant reminder commands.
- WorkManager reminders are reliable background work but Android may deliver them near, rather than exactly at, the requested minute on some devices.
- Open-ended AI requires the included backend to be deployed.
- Public launch should add backend authentication/rate limiting before many users use AI.
- Calendar/Gmail integrations, voice, recurring tasks, widgets, cloud sync and shared family features are planned for later phases.
