import { useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { searchStocks } from "../api/stockApi";
import { createTrade } from "../api/tradeApi";

export default function AddTradePage() {
  const { accountId } = useParams();
  const navigate = useNavigate();
  const { state } = useLocation();

  const [query, setQuery] = useState(state?.stock?.name ?? "");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedStock, setSelectedStock] = useState(state?.stock ?? null);

  const [type, setType] = useState("BUY");
  const [quantity, setQuantity] = useState("");
  const [price, setPrice] = useState("");
  const [tradedAt, setTradedAt] = useState("");
  const [error, setError] = useState(null);

  async function handleSearch(e) {
    e.preventDefault();
    if (!query.trim()) return;
    const results = await searchStocks(query);
    setSearchResults(results);
    setSelectedStock(null);
  }

  function handleSelectStock(stock) {
    setSelectedStock(stock);
    setSearchResults([]);
    setQuery(stock.name);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    if (!selectedStock) {
      setError("종목을 검색하여 선택해 주세요.");
      return;
    }
    try {
      await createTrade(accountId, {
        stockId: selectedStock.id,
        type,
        quantity,
        price,
        tradedAt: tradedAt ? tradedAt + ":00" : null,
      });
      navigate(`/accounts/${accountId}/portfolio`);
    } catch (err) {
      setError(err.response?.data?.message ?? "거래 등록에 실패했습니다.");
    }
  }

  return (
    <div style={{ maxWidth: 480, margin: "40px auto", padding: "0 16px" }}>
      <button onClick={() => navigate(`/accounts/${accountId}/portfolio`)} style={{ marginBottom: 16 }}>
        ← 포트폴리오
      </button>

      <h2>거래 등록</h2>

      {/* 종목 검색 */}
      <div style={{ marginBottom: 24 }}>
        <label style={{ display: "block", marginBottom: 4, fontWeight: "bold" }}>종목 검색</label>
        <form onSubmit={handleSearch} style={{ display: "flex", gap: 8 }}>
          <input
            value={query}
            onChange={(e) => { setQuery(e.target.value); setSelectedStock(null); }}
            placeholder="종목명 또는 티커 입력"
            style={{ flex: 1 }}
          />
          <button type="submit">검색</button>
        </form>

        {searchResults.length > 0 && (
          <ul style={{ border: "1px solid #dee2e6", borderRadius: 4, marginTop: 4, padding: 0, listStyle: "none" }}>
            {searchResults.map((stock) => (
              <li
                key={stock.id}
                onClick={() => handleSelectStock(stock)}
                style={{ padding: "8px 12px", cursor: "pointer", borderBottom: "1px solid #f1f3f5" }}
                onMouseEnter={(e) => e.currentTarget.style.background = "#f8f9fa"}
                onMouseLeave={(e) => e.currentTarget.style.background = "white"}
              >
                <span style={{ fontWeight: "bold" }}>{stock.name}</span>
                <span style={{ marginLeft: 8, color: "#888", fontSize: 13 }}>{stock.ticker}</span>
              </li>
            ))}
          </ul>
        )}

        {selectedStock && (
          <div style={{ marginTop: 8, padding: "8px 12px", background: "#e7f5ff", borderRadius: 4 }}>
            선택됨: <strong>{selectedStock.name}</strong> ({selectedStock.ticker})
          </div>
        )}
      </div>

      {/* 거래 폼 */}
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 16 }}>
          <label style={{ display: "block", marginBottom: 4, fontWeight: "bold" }}>거래 유형</label>
          <label style={{ marginRight: 16 }}>
            <input type="radio" value="BUY" checked={type === "BUY"} onChange={() => setType("BUY")} />
            {" "}매수
          </label>
          <label>
            <input type="radio" value="SELL" checked={type === "SELL"} onChange={() => setType("SELL")} />
            {" "}매도
          </label>
        </div>

        <div style={{ marginBottom: 16 }}>
          <label style={{ display: "block", marginBottom: 4, fontWeight: "bold" }}>수량</label>
          <input
            type="number"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            placeholder="예: 10"
            min="0"
            step="any"
            required
            style={{ width: "100%" }}
          />
        </div>

        <div style={{ marginBottom: 16 }}>
          <label style={{ display: "block", marginBottom: 4, fontWeight: "bold" }}>단가 (원)</label>
          <input
            type="number"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            placeholder="예: 54000"
            min="0"
            step="any"
            required
            style={{ width: "100%" }}
          />
        </div>

        <div style={{ marginBottom: 24 }}>
          <label style={{ display: "block", marginBottom: 4, fontWeight: "bold" }}>거래일시 (선택)</label>
          <input
            type="datetime-local"
            value={tradedAt}
            onChange={(e) => setTradedAt(e.target.value)}
            style={{ width: "100%" }}
          />
        </div>

        {error && <p style={{ color: "red", marginBottom: 12 }}>{error}</p>}

        <button type="submit" style={{ width: "100%", padding: "10px 0", fontWeight: "bold" }}>
          등록
        </button>
      </form>
    </div>
  );
}
