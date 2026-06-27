import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getPortfolio } from "../api/portfolioApi";
import { deleteAllTradesByStock } from "../api/tradeApi";

function formatKRW(value) {
  return Number(value).toLocaleString("ko-KR") + "원";
}

function formatRate(value) {
  const num = Number(value).toFixed(2);
  return num > 0 ? `+${num}%` : `${num}%`;
}

function ProfitText({ value }) {
  const color = value > 0 ? "#e03131" : value < 0 ? "#1971c2" : "#333";
  return <span style={{ color }}>{formatKRW(value)}</span>;
}

function RateText({ value }) {
  const color = value > 0 ? "#e03131" : value < 0 ? "#1971c2" : "#333";
  return <span style={{ color }}>{formatRate(value)}</span>;
}

export default function PortfolioPage() {
  const { accountId } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    getPortfolio(accountId)
      .then(setPortfolio)
      .catch(() => setError("포트폴리오를 불러오지 못했습니다."));
  }, [accountId]);

  if (error) return <p style={{ color: "red", padding: 24 }}>{error}</p>;
  if (!portfolio) return <p style={{ padding: 24 }}>불러오는 중...</p>;

  const { holdings, totalPurchaseAmount, totalEvaluationAmount, totalProfitLoss, totalProfitLossRate } = portfolio;

  async function handleDeleteHolding(e, stockId) {
    e.stopPropagation();
    if (!confirm("이 종목의 모든 거래 내역을 삭제할까요?")) return;
    await deleteAllTradesByStock(accountId, stockId);
    getPortfolio(accountId).then(setPortfolio);
  }

  return (
    <div style={{ maxWidth: 720, margin: "40px auto", padding: "0 16px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <button onClick={() => navigate("/accounts")}>← 계좌 목록</button>
        <button onClick={() => navigate(`/accounts/${accountId}/trades/new`)}>+ 거래 등록</button>
      </div>

      <h2>포트폴리오</h2>

      {/* 요약 */}
      <div style={{ display: "flex", gap: 24, marginBottom: 32, padding: 20, background: "#f8f9fa", borderRadius: 8 }}>
        <div>
          <div style={{ fontSize: 12, color: "#666" }}>총 매수금액</div>
          <div style={{ fontWeight: "bold" }}>{formatKRW(totalPurchaseAmount)}</div>
        </div>
        <div>
          <div style={{ fontSize: 12, color: "#666" }}>총 평가금액</div>
          <div style={{ fontWeight: "bold" }}>{formatKRW(totalEvaluationAmount)}</div>
        </div>
        <div>
          <div style={{ fontSize: 12, color: "#666" }}>총 수익금</div>
          <div style={{ fontWeight: "bold" }}><ProfitText value={totalProfitLoss} /></div>
        </div>
        <div>
          <div style={{ fontSize: 12, color: "#666" }}>총 수익률</div>
          <div style={{ fontWeight: "bold" }}><RateText value={totalProfitLossRate} /></div>
        </div>
      </div>

      {/* 종목 목록 */}
      {holdings.length === 0 ? (
        <p>보유 종목이 없습니다.</p>
      ) : (
        <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 14 }}>
          <thead>
            <tr style={{ borderBottom: "2px solid #dee2e6", textAlign: "right" }}>
              <th style={{ textAlign: "left", paddingBottom: 8 }}>종목</th>
              <th style={{ paddingBottom: 8 }}>보유수량</th>
              <th style={{ paddingBottom: 8 }}>평균매수가</th>
              <th style={{ paddingBottom: 8 }}>현재가</th>
              <th style={{ paddingBottom: 8 }}>수익금</th>
              <th style={{ paddingBottom: 8 }}>수익률</th>
              <th style={{ paddingBottom: 8 }}></th>
            </tr>
          </thead>
          <tbody>
            {holdings.map((h) => (
              <tr
                key={h.stockId}
                onClick={() => navigate(`/accounts/${accountId}/holdings/${h.stockId}`, { state: { holding: h } })}
                style={{ borderBottom: "1px solid #f1f3f5", textAlign: "right", cursor: "pointer" }}
                onMouseEnter={(e) => e.currentTarget.style.background = "#f8f9fa"}
                onMouseLeave={(e) => e.currentTarget.style.background = "white"}
              >
                <td style={{ textAlign: "left", padding: "12px 0" }}>
                  <div style={{ fontWeight: "bold" }}>{h.name}</div>
                  <div style={{ fontSize: 12, color: "#888" }}>{h.ticker}</div>
                </td>
                <td>{Number(h.quantity).toLocaleString()}</td>
                <td>{formatKRW(h.averagePrice)}</td>
                <td>{formatKRW(h.currentPrice)}</td>
                <td><ProfitText value={h.profitLoss} /></td>
                <td><RateText value={h.profitLossRate} /></td>
                <td>
                  <button
                    onClick={(e) => handleDeleteHolding(e, h.stockId)}
                    style={{ fontSize: 12, color: "#999", background: "none", border: "1px solid #ddd", borderRadius: 4, padding: "2px 8px", cursor: "pointer" }}
                  >
                    삭제
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
