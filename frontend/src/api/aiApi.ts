import { axiosInstance } from './axiosInstance';
import { AiSearchResponse } from '../types/ai.types';

export const aiApi = {
  processAiSearch: async (prompt: string): Promise<AiSearchResponse> => {
    const response = await axiosInstance.post<AiSearchResponse>('/ai/search', { prompt });
    return response.data;
  },
};
