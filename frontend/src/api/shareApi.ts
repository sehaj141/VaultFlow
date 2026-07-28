import { axiosInstance } from './axiosInstance';
import { CreateShareLinkPayload, PublicSharedResource, ShareLinkResponse } from '../types/share.types';

export const shareApi = {
  createShareLink: async (payload: CreateShareLinkPayload): Promise<ShareLinkResponse> => {
    const response = await axiosInstance.post<ShareLinkResponse>('/shares', payload);
    return response.data;
  },

  getPublicSharedResource: async (token: string): Promise<PublicSharedResource> => {
    const response = await axiosInstance.get<PublicSharedResource>(`/shares/public/${token}`);
    return response.data;
  },

  verifyPasswordAndAccess: async (token: string, password: string): Promise<PublicSharedResource> => {
    const response = await axiosInstance.post<PublicSharedResource>(`/shares/public/${token}/access`, { password });
    return response.data;
  },

  downloadSharedFile: async (token: string, password?: string, fileName?: string): Promise<void> => {
    const params = password ? { password } : {};
    const response = await axiosInstance.get(`/shares/public/${token}/download`, {
      params,
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName || 'download');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  revokeShareLink: async (token: string): Promise<void> => {
    await axiosInstance.delete(`/shares/${token}`);
  },
};
