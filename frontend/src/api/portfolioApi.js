import { client } from "./client";

export async function getPortfolio(accountId) {
  const response = await client.get(`/api/accounts/${accountId}/portfolio`);
  return response.data.data;
}
