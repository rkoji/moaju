import { client } from "./client";

export async function getAccounts() {
  const response = await client.get("/api/accounts");
  return response.data.data;
}

export async function createAccount({ brokerName, nickname }) {
  const response = await client.post("/api/accounts", { brokerName, nickname });
  return response.data.data;
}

export async function deleteAccount(accountId) {
  await client.delete(`/api/accounts/${accountId}`);
}
