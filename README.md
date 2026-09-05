# LifeMate AI — Premium V4

LifeMate AI is a native Android daily-life assistant built with Kotlin + Jetpack Compose. V4 focuses on making the app feel much closer to a real premium productivity assistant while keeping the UI simple.

## What is new in V4
- Exact **date + time picker** for tasks
- **Daily / weekly / monthly recurring tasks**
- Recurring tasks automatically move to their next occurrence when completed
- Upgraded **7-day planner strip** with date switching
- **Voice input** in the AI screen through the device speech recognizer
- AI can now create tasks and notes when the user clearly asks it to
- AI receives current time, timezone, open tasks, habits and recent notes as context
- Secure AI actions are returned from the backend and applied by the Android app
- Better AI prompt chips and loading state
- Premium Android splash screen
- Improved repeat/reminder indicators in task cards
- Version updated to **4.0.0**

## Core features
- Tasks with priority, notes, exact schedule and local reminder
- Repeating tasks: daily, weekly, monthly
- Daily planner grouped into morning / afternoon / evening
- Notes with search, edit, pin and delete
- Habits with daily completion and streaks
- Offline assistant commands
- Secure OpenAI-powered assistant through your own HTTPS backend
- AI task/note creation
- Voice-to-text input where a speech recognition service is available on the device
- Light / dark / system themes
- Local-first storage
- GitHub Actions for APK and AAB

## Example commands
### Offline
- `Add task buy groceries`
- `What do I have today?`
- `Plan my day`
- `Remind me to call Sam tomorrow at 6 pm`
- `Kal 7 pm baje assignment submit karna yaad dilana`

### With AI connected
- `Add a high priority task to submit the report tomorrow at 5 pm`
- `Remind me every week on Monday to review my goals`
- `Save this idea as a note: ...`
- `Look at my open tasks and tell me what I should do next`
- `Plan a realistic day from my current tasks`

## AI architecture
Android app → your HTTPS Cloudflare Worker → OpenAI Responses API.

The OpenAI API key is never packaged inside the APK/AAB. The included backend is in:

`backend/cloudflare-worker/`

The backend defaults to `gpt-5.6-luna` for a cost-sensitive daily assistant. The model can be changed through the backend environment configuration.

## GitHub build
The included workflow uses JDK 17 and Gradle 8.13 and builds:

```bash
gradle :app:assembleDebug
gradle :app:bundleRelease
```

The project uses Android Gradle Plugin 8.11.1 and targets Android API 36.

## Optional: connect AI automatically in GitHub builds
After deploying your backend, add this GitHub Actions repository secret:

`AI_BACKEND_URL`

Set its value to your HTTPS Worker URL. New APK/AAB builds will start with that URL already configured.

## Security
Never put `OPENAI_API_KEY` inside Android source code, Gradle files, GitHub source, or the APK.

For a public launch, add backend authentication, rate limiting and abuse protection before giving the app to a large number of users.
