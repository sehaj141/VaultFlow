import { axiosInstance } from './axiosInstance';
import { DashboardAnalytics } from '../types/dashboard.types';

export const dashboardApi = {
  getAnalytics: async (): Promise<DashboardAnalytics> => {
    const response = await axiosInstance.get<DashboardAnalytics>('/dashboard/analytics');
    return response.data;
  },
};
