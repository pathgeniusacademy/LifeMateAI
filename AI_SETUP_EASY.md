# Turn on real AI in LifeMate

LifeMate V3 already contains the Android AI client and the secure backend code. The only thing that cannot be bundled into a public APK is your private OpenAI API key.

## You need
- an OpenAI API key
- a Cloudflare account
- the Worker URL created from the included backend

## Backend folder
`backend/cloudflare-worker/`

Follow that folder's README to deploy it. Once deployed, you will receive an HTTPS URL similar to:

`https://lifemate-ai.<your-subdomain>.workers.dev`

## Connect it in the app
1. Open LifeMate.
2. Settings → AI connection.
3. Paste the Worker URL only — not the API key.
4. Tap **Test AI connection**.
5. When it says AI is ready, tap **Save settings**.
6. Open the AI tab and chat normally.

## Make future GitHub builds automatically connected
In your GitHub repository:

Settings → Secrets and variables → Actions → New repository secret

Name: `AI_BACKEND_URL`

Value: your Worker HTTPS URL

Run the Android build again. New APK/AAB builds will start with the AI backend URL already configured.

## Important
Keep `OPENAI_API_KEY` only on the server. Do not paste it into Android source code or GitHub files.
