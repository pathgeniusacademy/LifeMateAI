# LifeMate AI secure backend

This backend keeps the OpenAI API key on the server. Never put the API key inside the Android app or GitHub source.

## Deploy outline
1. Create a Cloudflare Worker project from this folder.
2. Copy `wrangler.toml.example` to `wrangler.toml`.
3. Add the API key as a secret: `npx wrangler secret put OPENAI_API_KEY`.
4. Deploy with `npm run deploy`.
5. Copy the resulting HTTPS Worker URL into LifeMate AI > Settings > Backend URL.

For a public launch, add authentication and rate limiting so strangers cannot use your backend at your expense.

The Worker sends `store: false` with Responses API requests and returns only the assistant reply to the Android app.
