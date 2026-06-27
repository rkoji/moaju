import { client } from "./client";

export async function searchStocks(q) {
  const response = await client.get("/api/stocks", { params: { q } });
  return response.data.data;
}
