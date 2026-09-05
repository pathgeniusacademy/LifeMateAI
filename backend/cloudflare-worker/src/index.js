const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status,
  headers: {
    "content-type": "application/json; charset=utf-8",
    "access-control-allow-origin": "*",
    "access-control-allow-headers": "content-type",
    "access-control-allow-methods": "GET,POST,OPTIONS"
  }
});

function compactContext(context) {
  const tasks = Array.isArray(context?.tasks) ? context.tasks.slice(0, 30) : [];
  const habits = Array.isArray(context?.habits) ? context.habits.slice(0, 20) : [];
  const notes = Array.isArray(context?.notes) ? context.notes.slice(0, 8) : [];
  return {
    open_tasks: tasks.map(t => ({
      title: String(t?.title || "").slice(0, 200),
      notes: String(t?.notes || "").slice(0, 400),
      dueAt: t?.dueAt ?? null,
      priority: String(t?.priority || "MEDIUM").slice(0, 20)
    })),
    habits: habits.map(h => ({
      name: String(h?.name || "").slice(0, 160),
      streak: Number(h?.streak || 0),
      lastCompletedDate: h?.lastCompletedDate ?? null
    })),
    recent_notes: notes.map(n => ({
      title: String(n?.title || "").slice(0, 160),
      body: String(n?.body || "").slice(0, 500)
    }))
  };
}

function extractText(data) {
  if (typeof data?.output_text === "string" && data.output_text.trim()) {
    return data.output_text.trim();
  }
  return (data?.output || [])
    .flatMap(item => Array.isArray(item?.content) ? item.content : [])
    .filter(part => part?.type === "output_text" && typeof part.text === "string")
    .map(part => part.text)
    .join("\n")
    .trim();
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return json({ ok: true });
    const url = new URL(request.url);
    const model = env.OPENAI_MODEL || "gpt-5.6-luna";

    if (url.pathname === "/health" && request.method === "GET") {
      return json({ ok: true, service: "lifemate-ai", model });
    }
    if (url.pathname !== "/v1/assistant" || request.method !== "POST") {
      return json({ error: "Not found" }, 404);
    }
    if (!env.OPENAI_API_KEY) return json({ error: "Server is missing OPENAI_API_KEY" }, 500);

    let body;
    try { body = await request.json(); } catch { return json({ error: "Invalid JSON" }, 400); }
    const message = String(body?.message || "").trim();
    if (!message) return json({ error: "Message is required" }, 400);
    if (message.length > 8000) return json({ error: "Message too long" }, 413);

    const history = Array.isArray(body?.history) ? body.history.slice(-12) : [];
    const userContext = compactContext(body?.context || {});
    const contextText = JSON.stringify(userContext).slice(0, 16000);

    const input = history
      .filter(m => m && (m.role === "user" || m.role === "assistant") && typeof m.text === "string")
      .map(m => ({ role: m.role, content: m.text.slice(0, 5000) }));

    input.push({
      role: "user",
      content: `Current LifeMate context (may be incomplete; use only when relevant): ${contextText}\n\nUser message: ${message}`
    });

    const payload = {
      model,
      store: false,
      reasoning: { effort: "low" },
      instructions: [
        "You are LifeMate, a warm, concise daily-life planning assistant inside an Android organizer app.",
        "Help with planning, prioritization, tasks, reminders, notes, routines, writing, study organization, and everyday decisions.",
        "The app may send current open tasks, habits and recent notes as context. Use that context when it actually helps, and do not invent missing details.",
        "Keep most responses short, skimmable and actionable. Use bullets only when useful.",
        "When the user asks what to do next, prioritize urgent/high-priority/due-soon tasks and avoid overwhelming them.",
        "Do not claim that you created, changed, sent, purchased, booked, or deleted anything unless the Android app itself confirms that action.",
        "For simple reminder/task creation, tell the user they can phrase it naturally; the app's offline command layer may create it.",
        "Do not request passwords, payment-card details, API keys, or unnecessary sensitive personal data."
      ].join(" "),
      input,
      max_output_tokens: 1000
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

    let data;
    try { data = await upstream.json(); } catch { return json({ error: "Invalid AI provider response" }, 502); }
    if (!upstream.ok) {
      console.error("OpenAI error", upstream.status, JSON.stringify(data).slice(0, 1500));
      return json({ error: "AI provider error" }, 502);
    }

    const reply = extractText(data);
    if (!reply) return json({ error: "Empty AI response" }, 502);
    return json({ reply, model });
  }
};
