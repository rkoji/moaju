import { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { getTradesByStock, deleteTrade } from "../api/tradeApi";

function formatKRW(value) {
  if (value == null) return "-";
  return Number(value).toLocaleString("ko-KR") + "원";
}

function formatRate(value) {
  if (value == null) return "-";
  const num = Number(value).toFixed(2);
  return num > 0 ? `+${num}%` : `${num}%`;
}

function ProfitText({ value }) {
  if (value == null) return <span>-</span>;
  const color = value > 0 ? "#e03131" : value < 0 ? "#1971c2" : "#333";
  return <span style={{ color }}>{formatKRW(value)}</span>;
}

function RateText({ value }) {
  if (value == null) return <span>-</span>;
  const color = value > 0 ? "#e03131" : value < 0 ? "#1971c2" : "#333";
  return <span style={{ color }}>{formatRate(value)}</span>;
}

export default function HoldingDetailPage() {
  const { accountId, stockId } = useParams();
  const navigate = useNavigate();
  const { state } = useLocation();
  const holding = state?.holding;

  const [trades, setTrades] = useState([]);
  const [error, setError] = useState(null);

  async function loadTrades() {
    try {
      const data = await getTradesByStock(accountId, stockId);
      setTrades(data.sort((a, b) => new Date(b.tradedAt) - new Date(a.tradedAt)));
    } catch {
      setError("거래 내역을 불러오지 못했습니다.");
    }
  }

  useEffect(() => {
    loadTrades();
  }, [accountId, stockId]);

  async function handleDelete(tradeId) {
    if (!confirm("이 거래 내역을 삭제할까요?")) return;
    await deleteTrade(accountId, tradeId);
    navigate(`/accounts/${accountId}/portfolio`);
  }

  return (
    <div style={{ maxWidth: 600, margin: "40px auto", padding: "0 16px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <button onClick={() => navigate(`/accounts/${accountId}/portfolio`)}>← 포트폴리오</button>
        <button
          onClick={() => navigate(`/accounts/${accountId}/trades/new`, { state: { stock: { id: Number(stockId), name: holding?.name, ticker: holding?.ticker } } })}
        >
          + 거래 추가
        </button>
      </div>

      {/* 종목 헤더 */}
      <div style={{ marginBottom: 24 }}>
        <h2 style={{ margin: 0 }}>{holding?.name ?? "종목 상세"}</h2>
        <span style={{ color: "#888", fontSize: 14 }}>{holding?.ticker}</span>
      </div>

      {/* 보유 요약 */}
      {holding && (
        <div style={{ display: "flex", gap: 20, padding: 16, background: "#f8f9fa", borderRadius: 8, marginBottom: 32, flexWrap: "wrap" }}>
          <div>
            <div style={{ fontSize: 12, color: "#666" }}>보유 수량</div>
            <div style={{ fontWeight: "bold" }}>{Number(holding.quantity).toLocaleString()}주</div>
          </div>
          <div>
            <div style={{ fontSize: 12, color: "#666" }}>평균 매수가</div>
            <div style={{ fontWeight: "bold" }}>{formatKRW(holding.averagePrice)}</div>
          </div>
          <div>
            <div style={{ fontSize: 12, color: "#666" }}>현재가</div>
            <div style={{ fontWeight: "bold" }}>{formatKRW(holding.currentPrice)}</div>
          </div>
          <div>
            <div style={{ fontSize: 12, color: "#666" }}>실현손익</div>
            <div style={{ fontWeight: "bold" }}><ProfitText value={holding.realizedProfitLoss} /></div>
          </div>
          <div>
            <div style={{ fontSize: 12, color: "#666" }}>평가손익</div>
            <div style={{ fontWeight: "bold" }}><ProfitText value={holding.evaluationProfitLoss} /></div>
          </div>
          <div>
            <div style={{ fontSize: 12, color: "#666" }}>총손익</div>
            <div style={{ fontWeight: "bold" }}><ProfitText value={holding.profitLoss} /></div>
          </div>
          <div>
            <div style={{ fontSize: 12, color: "#666" }}>총수익률</div>
            <div style={{ fontWeight: "bold" }}><RateText value={holding.profitLossRate} /></div>
          </div>
        </div>
      )}

      {/* 거래 내역 */}
      <h3 style={{ marginBottom: 12 }}>거래 내역</h3>

      {error && <p style={{ color: "red" }}>{error}</p>}

      {trades.length === 0 ? (
        <p style={{ color: "#888" }}>거래 내역이 없습니다.</p>
      ) : (
        <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
          {trades.map((trade) => (
            <li key={trade.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "12px 0", borderBottom: "1px solid #f1f3f5" }}>
              <div>
                <span style={{
                  fontWeight: "bold",
                  color: trade.type === "BUY" ? "#e03131" : "#1971c2",
                  marginRight: 8,
                }}>
                  {trade.type === "BUY" ? "매수" : "매도"}
                </span>
                <span style={{ fontSize: 13, color: "#555" }}>
                  {Number(trade.quantity).toLocaleString()}주 · {formatKRW(trade.price)}
                </span>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <span style={{ fontSize: 12, color: "#aaa" }}>
                  {trade.tradedAt ? new Date(trade.tradedAt).toLocaleDateString("ko-KR") : "-"}
                </span>
                <button
                  onClick={() => handleDelete(trade.id)}
                  style={{ fontSize: 12, color: "#999", background: "none", border: "1px solid #ddd", borderRadius: 4, padding: "2px 8px", cursor: "pointer" }}
                >
                  삭제
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
