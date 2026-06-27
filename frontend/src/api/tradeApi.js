import { client } from "./client";

export async function createTrade(accountId, { stockId, type, quantity, price, tradedAt }) {
  const response = await client.post(`/api/accounts/${accountId}/trades`, {
    stockId,
    type,
    quantity: Number(quantity),
    price: Number(price),
    tradedAt: tradedAt || null,
  });
  return response.data.data;
}

export async function getTradesByStock(accountId, stockId) {
  const response = await client.get(`/api/accounts/${accountId}/trades/stock/${stockId}`);
  return response.data.data;
}

export async function deleteTrade(accountId, tradeId) {
  await client.delete(`/api/accounts/${accountId}/trades/${tradeId}`);
}

export async function deleteAllTradesByStock(accountId, stockId) {
  await client.delete(`/api/accounts/${accountId}/trades/stock/${stockId}`);
}
