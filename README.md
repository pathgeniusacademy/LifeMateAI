# LifeMate AI — V5 Focus Edition

Native Android organizer, Kotlin + Jetpack Compose.

## New in V5
- Teal / ink visual system, redesigned home, purposeful quick actions and attention queue including overdue tasks.
- Focus Studio: 15, 25 or 50 minute sessions; pause, resume, confirmed reset and persisted deadline. Completed sessions populate daily focus totals. No background alarm.
- Seven-day focus bars and actual recorded habit check-ins, with legacy streak migration.
- Search task title/details, overdue filter, priority sorting, tap-to-edit and confirmed deletion.
- Chat scrolls to new messages; conversation clearing asks for confirmation.
- Compact-screen task and note dialogs scroll, bottom navigation respects system insets.
- Reminder disable cancels queued reminders; enable reschedules future reminders. Worker also checks current task/settings.
- Existing local data and package ID retained. Version 5.0.0 (code 5).

## Build and verification
Read START_HERE.txt for GitHub upload and APK/AAB steps. Workflows run unit tests before builds.
Android SDK/Gradle were unavailable in the editing environment, so compilation, instrumentation and visual device testing remain required. Source/archive checks are not a successful APK build.

## Existing capabilities and setup
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
