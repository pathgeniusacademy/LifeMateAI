# LifeMate AI V4 secure backend

This Cloudflare Worker keeps the OpenAI API key on the server and gives the Android app a secure HTTPS endpoint.

V4 also supports safe app-side actions for explicit user requests:
- create a task
- create a note

The Android app validates the returned action before saving it locally.

## Setup
1. Install Node.js.
2. Open this folder in a terminal.
3. Run `npm install`.
4. Run `npx wrangler login`.
5. Copy `wrangler.toml.example` to `wrangler.toml`.
6. Run `npx wrangler secret put OPENAI_API_KEY`.
7. Paste your OpenAI API key when Wrangler asks for it.
8. Run `npm run deploy`.
9. Copy the HTTPS Worker URL.
10. In LifeMate: Settings → AI connection → paste URL → Test AI connection → Save.

Default model: `gpt-5.6-luna`.

## Public launch warning
Before giving the app to a large public audience, add authentication, rate limiting and abuse protection. Otherwise anyone who discovers your public Worker endpoint could consume your API budget.
