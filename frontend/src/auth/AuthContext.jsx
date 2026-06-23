import { createContext, useContext, useEffect, useState } from "react";
import * as userApi from "../api/userApi";
import { setAccessToken } from "../api/tokenStore";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isRestoring, setIsRestoring] = useState(true);

  useEffect(() => {
    userApi
      .reissue()
      .then((accessToken) => {
        setAccessToken(accessToken);
        setIsLoggedIn(true);
      })
      .catch(() => {
        setAccessToken(null);
        setIsLoggedIn(false);
      })
      .finally(() => setIsRestoring(false));
  }, []);

  async function login(email, password) {
    const accessToken = await userApi.login({ email, password });
    setAccessToken(accessToken);
    setIsLoggedIn(true);
  }

  async function logout() {
    await userApi.logout();
    setAccessToken(null);
    setIsLoggedIn(false);
  }

  return (
    <AuthContext.Provider value={{ isLoggedIn, isRestoring, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
