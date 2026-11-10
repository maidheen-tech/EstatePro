const TOKEN_KEY = "estatepro.access-token";

export const authToken = () => sessionStorage.getItem(TOKEN_KEY);
export const clearSession = () => {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem("estatepro.user");
};
export const savedUser = () => {
  try { return JSON.parse(sessionStorage.getItem("estatepro.user")); } catch { return null; }
};
export const saveSession = (session) => {
  sessionStorage.setItem(TOKEN_KEY, session.token);
  sessionStorage.setItem("estatepro.user", JSON.stringify(session.user));
};

export async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !(options.body instanceof FormData)) headers.set("Content-Type", "application/json");
  if (authToken()) headers.set("Authorization", `Bearer ${authToken()}`);
  const response = await fetch(`/api${path}`, { ...options, headers });
  if (response.status === 204) return null;
  const type = response.headers.get("content-type") || "";
  const data = type.includes("application/json") ? await response.json() : await response.text();
  if (!response.ok) {
    const message = typeof data === "object" ? data.error || data.message || "The request could not be completed." : data;
    const error = new Error(message || `Request failed (${response.status})`);
    error.status = response.status;
    error.fields = data?.fields;
    throw error;
  }
  return data;
}

export const post = (path, body) => api(path, { method: "POST", body: JSON.stringify(body) });
export const patch = (path, body) => api(path, { method: "PATCH", body: JSON.stringify(body) });
export const put = (path, body) => api(path, { method: "PUT", body: JSON.stringify(body) });
export const remove = (path) => api(path, { method: "DELETE" });
