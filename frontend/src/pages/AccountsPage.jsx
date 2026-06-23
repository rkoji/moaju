import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAccounts, createAccount, deleteAccount } from "../api/accountApi";
import { useAuth } from "../auth/AuthContext";

export default function AccountsPage() {
  const [accounts, setAccounts] = useState([]);
  const [brokerName, setBrokerName] = useState("");
  const [nickname, setNickname] = useState("");
  const [error, setError] = useState(null);
  const { logout } = useAuth();
  const navigate = useNavigate();

  async function loadAccounts() {
    const data = await getAccounts();
    setAccounts(data);
  }

  useEffect(() => {
    loadAccounts().catch(() => setError("계좌 목록을 불러오지 못했습니다."));
  }, []);

  async function handleCreate(e) {
    e.preventDefault();
    setError(null);
    try {
      await createAccount({ brokerName, nickname });
      setBrokerName("");
      setNickname("");
      await loadAccounts();
    } catch (err) {
      setError(err.response?.data?.message ?? "계좌 등록에 실패했습니다.");
    }
  }

  async function handleDelete(accountId) {
    await deleteAccount(accountId);
    await loadAccounts();
  }

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  return (
    <div style={{ maxWidth: 480, margin: "40px auto" }}>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <h2>내 계좌</h2>
        <button onClick={handleLogout}>로그아웃</button>
      </div>

      <form onSubmit={handleCreate} style={{ marginBottom: 24 }}>
        <input
          placeholder="증권사명 (예: 삼성증권)"
          value={brokerName}
          onChange={(e) => setBrokerName(e.target.value)}
          required
        />
        <input
          placeholder="계좌 별칭"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          required
          style={{ marginLeft: 8 }}
        />
        <button type="submit" style={{ marginLeft: 8 }}>
          계좌 등록
        </button>
      </form>

      {error && <p style={{ color: "red" }}>{error}</p>}

      {accounts.length === 0 ? (
        <p>등록된 계좌가 없습니다.</p>
      ) : (
        <ul>
          {accounts.map((account) => (
            <li key={account.id} style={{ marginBottom: 8 }}>
              [{account.brokerName}] {account.nickname}
              <button onClick={() => handleDelete(account.id)} style={{ marginLeft: 8 }}>
                삭제
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
