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
    now_iso: String(context?.nowIso || "").slice(0, 80),
    timezone: String(context?.timezone || "").slice(0, 80),
    open_tasks: tasks.map(t => ({
      id: String(t?.id || "").slice(0, 80),
      title: String(t?.title || "").slice(0, 200),
      notes: String(t?.notes || "").slice(0, 400),
      dueAt: t?.dueAt ?? null,
      priority: String(t?.priority || "MEDIUM").slice(0, 20),
      repeat: String(t?.repeat || "NONE").slice(0, 20)
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
  if (typeof data?.output_text === "string" && data.output_text.trim()) return data.output_text.trim();
  return (data?.output || [])
    .flatMap(item => Array.isArray(item?.content) ? item.content : [])
    .filter(part => part?.type === "output_text" && typeof part.text === "string")
    .map(part => part.text)
    .join("\n")
    .trim();
}

function parseFunctionActions(data) {
  const actions = [];
  for (const item of (data?.output || [])) {
    if (item?.type !== "function_call" || typeof item?.name !== "string") continue;
    let args = {};
    try { args = JSON.parse(item.arguments || "{}"); } catch { continue; }

    if (item.name === "create_task") {
      const title = String(args.title || "").trim().slice(0, 200);
      if (!title) continue;
      let dueAt = null;
      if (args.due_at_iso) {
        const parsed = Date.parse(String(args.due_at_iso));
        if (Number.isFinite(parsed)) dueAt = parsed;
      }
      const priority = ["LOW", "MEDIUM", "HIGH"].includes(String(args.priority || "").toUpperCase())
        ? String(args.priority).toUpperCase() : "MEDIUM";
      const repeat = ["NONE", "DAILY", "WEEKLY", "MONTHLY"].includes(String(args.repeat || "").toUpperCase())
        ? String(args.repeat).toUpperCase() : "NONE";
      actions.push({
        type: "create_task",
        title,
        notes: String(args.notes || "").slice(0, 1200),
        dueAt,
        priority,
        reminderEnabled: Boolean(args.reminder_enabled && dueAt),
        repeat
      });
    }

    if (item.name === "create_note") {
      const title = String(args.title || "AI note").trim().slice(0, 200) || "AI note";
      const body = String(args.body || "").slice(0, 4000);
      if (!body && !title) continue;
      actions.push({ type: "create_note", title, body });
    }
  }
  return actions.slice(0, 3);
}

function fallbackActionReply(actions) {
  if (!actions.length) return "Done.";
  if (actions.length === 1 && actions[0].type === "create_task") return `Done — I added “${actions[0].title}” to your tasks.`;
  if (actions.length === 1 && actions[0].type === "create_note") return `Done — I saved “${actions[0].title}” as a note.`;
  return `Done — I made ${actions.length} updates in LifeMate.`;
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return json({ ok: true });
    const url = new URL(request.url);
    const model = env.OPENAI_MODEL || "gpt-5.6-luna";

    if (url.pathname === "/health" && request.method === "GET") {
      return json({ ok: true, service: "lifemate-ai", model, actions: true });
    }
    if (url.pathname !== "/v1/assistant" || request.method !== "POST") return json({ error: "Not found" }, 404);
    if (!env.OPENAI_API_KEY) return json({ error: "Server is missing OPENAI_API_KEY" }, 500);

    let body;
    try { body = await request.json(); } catch { return json({ error: "Invalid JSON" }, 400); }
    const message = String(body?.message || "").trim();
    if (!message) return json({ error: "Message is required" }, 400);
    if (message.length > 8000) return json({ error: "Message too long" }, 413);

    const history = Array.isArray(body?.history) ? body.history.slice(-12) : [];
    const userContext = compactContext(body?.context || {});
    const contextText = JSON.stringify(userContext).slice(0, 18000);

    const input = history
      .filter(m => m && (m.role === "user" || m.role === "assistant") && typeof m.text === "string")
      .map(m => ({ role: m.role, content: m.text.slice(0, 5000) }));

    input.push({
      role: "user",
      content: `Current LifeMate context: ${contextText}\n\nUser message: ${message}`
    });

    const tools = [
      {
        type: "function",
        name: "create_task",
        description: "Create a task in LifeMate only when the user clearly asks to add, remember, schedule, or create a task/reminder.",
        parameters: {
          type: "object",
          properties: {
            title: { type: "string", description: "Short task title." },
            notes: { type: "string", description: "Optional details, otherwise empty string." },
            due_at_iso: { type: "string", description: "ISO 8601 date-time with offset when the user supplied or implied a due time; otherwise empty string." },
            priority: { type: "string", enum: ["LOW", "MEDIUM", "HIGH"] },
            reminder_enabled: { type: "boolean" },
            repeat: { type: "string", enum: ["NONE", "DAILY", "WEEKLY", "MONTHLY"] }
          },
          required: ["title", "notes", "due_at_iso", "priority", "reminder_enabled", "repeat"],
          additionalProperties: false
        }
      },
      {
        type: "function",
        name: "create_note",
        description: "Create a note in LifeMate only when the user clearly asks to save, capture, or turn something into a note.",
        parameters: {
          type: "object",
          properties: {
            title: { type: "string" },
            body: { type: "string" }
          },
          required: ["title", "body"],
          additionalProperties: false
        }
      }
    ];

    const payload = {
      model,
      store: false,
      reasoning: { effort: "low" },
      instructions: [
        "You are LifeMate, a warm, concise daily-life planning assistant inside an Android organizer app.",
        "Help with planning, prioritization, tasks, reminders, notes, routines, writing, study organization, and everyday decisions.",
        "Use the supplied current time and timezone for relative dates such as today, tomorrow, next Monday, or this evening.",
        "Use create_task or create_note only when the user clearly asks LifeMate to create or save something. Do not call a tool merely for advice.",
        "If creating a reminder, include a due_at_iso and set reminder_enabled true. If the user did not give a time, do not invent an exact clock time; leave due_at_iso empty unless a reasonable day-only task is clearly requested.",
        "For recurring requests, use DAILY, WEEKLY, or MONTHLY only when the user explicitly requests recurrence.",
        "Keep most responses short, friendly, skimmable and actionable.",
        "Do not claim an app action succeeded unless you called the corresponding tool in this response.",
        "Do not request passwords, payment-card details, API keys, or unnecessary sensitive personal data."
      ].join(" "),
      input,
      tools,
      tool_choice: "auto",
      max_output_tokens: 1200
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
      console.error("OpenAI error", upstream.status, JSON.stringify(data).slice(0, 1800));
      return json({ error: "AI provider error" }, 502);
    }

    const actions = parseFunctionActions(data);
    const reply = extractText(data) || fallbackActionReply(actions);
    return json({ reply, actions, model });
  }
};
