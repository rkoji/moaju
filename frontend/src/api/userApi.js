import { client } from "./client";

export async function signUp({ email, password, nickname }) {
  await client.post("/api/users/signup", { email, password, nickname });
}

export async function login({ email, password }) {
  const response = await client.post("/api/users/login", { email, password });
  return response.data.data;
}

export async function logout() {
  await client.post("/api/users/logout");
}

export async function reissue() {
  const response = await client.post("/api/users/reissue");
  return response.data.data;
}
