import { Link } from "react-router-dom";
import MarketSummarySection from "../components/MarketSummarySection";
import { useAuth } from "../auth/AuthContext";
import "./HomePage.css";

export default function HomePage() {
  const { isLoggedIn } = useAuth();

  return (
    <div className="home">
      <header className="home-header">
        <span className="home-logo">모아주</span>
        <nav className="home-nav">
          {isLoggedIn ? (
            <Link to="/accounts" className="primary">
              내 계좌
            </Link>
          ) : (
            <>
              <Link to="/login" className="ghost">
                로그인
              </Link>
              <Link to="/signup" className="primary">
                회원가입
              </Link>
            </>
          )}
        </nav>
      </header>

      <MarketSummarySection />
    </div>
  );
}
