import { useEffect, useState } from "react";
import { getLatestMarketSummary } from "../api/marketSummaryApi";
import "./MarketSummarySection.css";

function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function formatDateTime(dateTimeString) {
  const date = new Date(dateTimeString);
  return date.toLocaleString("ko-KR", {
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function MarketSummarySection({ compact = false }) {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getLatestMarketSummary()
      .then(setSummary)
      .catch((err) => {
        setError(err.response?.data?.message ?? "시장 요약을 불러오지 못했습니다.");
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <p className="market-summary-status">불러오는 중...</p>;
  }

  if (error) {
    return <p className="market-summary-status error">{error}</p>;
  }

  if (!summary) {
    return null;
  }

  return (
    <section className={compact ? "market-summary compact" : "market-summary"}>
      {!compact && (
        <>
          <span className="market-summary-eyebrow">AI 시장 요약</span>
          <h2>오늘의 증권가 소식</h2>
        </>
      )}

      <p className="market-summary-meta">
        {formatDate(summary.date)} 기준 · {formatDateTime(summary.createdAt)} 생성
      </p>

      <div className="market-summary-card">
        <p>{summary.summary}</p>
      </div>

      {!compact && (
        <>
          <h3 className="market-summary-news-title">주요 뉴스</h3>
          <ul className="market-summary-news-list">
            {summary.news.map((item, index) => (
              <li key={index}>
                <a
                  href={item.link}
                  target="_blank"
                  rel="noreferrer"
                  className="market-summary-news-item"
                >
                  <div className="market-summary-news-item-title">{item.title}</div>
                  <div className="market-summary-news-item-summary">{item.summary}</div>
                </a>
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  );
}
