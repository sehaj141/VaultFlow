import axiosInstance from "./axiosInstance";
import { Dashboard } from "../types/dashboard.types";

export const dashboardApi = {
  get: () => axiosInstance.get<Dashboard>("/dashboard"),
};