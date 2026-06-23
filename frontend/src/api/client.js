import axios from "axios";
import { getAccessToken, setAccessToken } from "./tokenStore";

const BASE_URL = "http://localhost:8080";

export const client = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
});

client.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isReissuing = false;

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error;

    if (response?.status !== 401 || config.url === "/api/users/reissue" || config._retried) {
      return Promise.reject(error);
    }

    if (isReissuing) {
      return Promise.reject(error);
    }

    config._retried = true;
    isReissuing = true;

    try {
      const reissueResponse = await axios.post(
        `${BASE_URL}/api/users/reissue`,
        null,
        { withCredentials: true }
      );
      const newAccessToken = reissueResponse.data.data;
      setAccessToken(newAccessToken);
      config.headers.Authorization = `Bearer ${newAccessToken}`;
      return client(config);
    } catch (reissueError) {
      setAccessToken(null);
      return Promise.reject(reissueError);
    } finally {
      isReissuing = false;
    }
  }
);
