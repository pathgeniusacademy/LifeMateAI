# LifeMate AI V4 — Easy AI Setup

V4 already contains the Android AI client and the secure backend code. The only private thing you must add yourself is the OpenAI API key on the server.

## What you need
- An OpenAI API key
- A Cloudflare account
- The included folder: `backend/cloudflare-worker/`

## Deploy the backend
1. Open `backend/cloudflare-worker/` on your computer.
2. Follow its README.
3. Add `OPENAI_API_KEY` as a Cloudflare Worker secret.
4. Deploy the Worker.
5. Copy the HTTPS Worker URL.

## Connect the Android app
1. Open LifeMate AI.
2. Open **Settings**.
3. Under **AI connection**, paste only the HTTPS Worker URL.
4. Tap **Test AI connection**.
5. Tap **Save settings**.
6. Open the AI tab.

Now LifeMate can provide open-ended help and can create a task or note when you clearly ask it to do so.

## Make GitHub APK/AAB builds already connected
GitHub repository → Settings → Secrets and variables → Actions → New repository secret

Name:
`AI_BACKEND_URL`

Value:
Your Worker HTTPS URL

Run the Android build again.

## Important
Do not paste `OPENAI_API_KEY` into the Android app, GitHub source files, `build.gradle`, or any public file.

Before releasing the app publicly to many users, protect the backend with authentication, rate limits and abuse controls so strangers cannot freely consume your API budget.
