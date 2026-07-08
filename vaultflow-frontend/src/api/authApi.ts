import axiosInstance from "./axiosInstance";
import { LoginRequest, RegisterRequest, AuthResponse } from "../types/auth.types";

export const authApi = {
  register: (data: RegisterRequest) =>
    axiosInstance.post<AuthResponse>("/auth/register", data),

  login: (data: LoginRequest) =>
    axiosInstance.post<AuthResponse>("/auth/login", data),

  logout: (refreshToken: string) =>
    axiosInstance.post("/auth/logout", { refreshToken }),
};