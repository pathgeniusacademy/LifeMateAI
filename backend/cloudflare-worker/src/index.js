const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status,
  headers: {
    "content-type": "application/json; charset=utf-8",
    "access-control-allow-origin": "*",
    "access-control-allow-headers": "content-type",
    "access-control-allow-methods": "POST,OPTIONS"
  }
});

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return json({ ok: true });
    const url = new URL(request.url);
    if (url.pathname === "/health") return json({ ok: true, service: "lifemate-ai" });
    if (url.pathname !== "/v1/assistant" || request.method !== "POST") {
      return json({ error: "Not found" }, 404);
    }
    if (!env.OPENAI_API_KEY) return json({ error: "Server is missing OPENAI_API_KEY" }, 500);

    let body;
    try { body = await request.json(); } catch { return json({ error: "Invalid JSON" }, 400); }
    const message = String(body?.message || "").trim();
    if (!message) return json({ error: "Message is required" }, 400);
    if (message.length > 8000) return json({ error: "Message too long" }, 413);

    const history = Array.isArray(body?.history) ? body.history.slice(-10) : [];
    const input = history
      .filter(m => m && (m.role === "user" || m.role === "assistant") && typeof m.text === "string")
      .map(m => ({ role: m.role, content: m.text.slice(0, 5000) }));
    input.push({ role: "user", content: message });

    const payload = {
      model: env.OPENAI_MODEL || "gpt-5.6-luna",
      store: false,
      instructions: [
        "You are LifeMate, a concise daily-life planning assistant.",
        "Help with planning, organization, notes, prioritization, routines, writing, and everyday decisions.",
        "Do not claim you created a task, reminder, calendar event, email, purchase, booking, or other action unless the app explicitly confirms that action separately.",
        "Prefer practical, short answers. Ask a clarifying question only when genuinely needed.",
        "Do not request secrets, passwords, payment-card details, or unnecessary sensitive personal data."
      ].join(" "),
      input,
      max_output_tokens: 900
    };

    let upstream;
    try {
      upstream = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${env.OPENAI_API_KEY}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });
    } catch {
      return json({ error: "Could not reach AI provider" }, 502);
    }

    const data = await upstream.json();
    if (!upstream.ok) {
      console.error("OpenAI error", upstream.status, JSON.stringify(data).slice(0, 1500));
      return json({ error: "AI provider error" }, 502);
    }

    const reply = (data.output || [])
      .flatMap(item => Array.isArray(item.content) ? item.content : [])
      .filter(part => part?.type === "output_text" && typeof part.text === "string")
      .map(part => part.text)
      .join("\n")
      .trim();

    if (!reply) return json({ error: "Empty AI response" }, 502);
    return json({ reply });
  }
};
