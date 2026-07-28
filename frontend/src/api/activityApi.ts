import { axiosInstance } from './axiosInstance';
import { ActivityLogItem } from '../types/activity.types';

export const activityApi = {
  getActivityFeed: async (limit: number = 20): Promise<ActivityLogItem[]> => {
    const response = await axiosInstance.get<ActivityLogItem[]>('/activities', {
      params: { limit },
    });
    return response.data;
  },
};
