# LifeMate AI secure backend

This is the secure AI bridge used by the Android app. The OpenAI API key stays on the server and is never packaged inside the APK/AAB.

## Fast setup
1. Create a Cloudflare account and install Node.js on your PC once.
2. Open this `backend/cloudflare-worker` folder in a terminal.
3. Run `npm install`.
4. Run `npx wrangler login`.
5. Copy `wrangler.toml.example` to `wrangler.toml`.
6. Run `npx wrangler secret put OPENAI_API_KEY` and paste your OpenAI API key when prompted.
7. Run `npm run deploy`.
8. Wrangler prints an HTTPS Worker URL. Put that URL in **LifeMate AI > Settings > AI connection** and tap **Test connection**.

The default model is `gpt-5.6-luna` for a cost-sensitive assistant. Change `OPENAI_MODEL` in `wrangler.toml` if desired.

For a public production launch, add authentication, abuse protection and rate limiting so unknown users cannot spend your API budget.
