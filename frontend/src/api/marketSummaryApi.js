import { client } from "./client";

export async function getLatestMarketSummary() {
  const response = await client.get("/api/market-summary/latest");
  return response.data.data;
}
