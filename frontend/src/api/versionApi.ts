import { axiosInstance } from './axiosInstance';
import { FileItem } from '../types/file.types';
import { FileVersionItem } from '../types/version.types';

export const versionApi = {
  getVersionTimeline: async (fileId: string): Promise<FileVersionItem[]> => {
    const response = await axiosInstance.get<FileVersionItem[]>(`/files/${fileId}/versions`);
    return response.data;
  },

  uploadNewVersion: async (fileId: string, file: File): Promise<FileItem> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axiosInstance.post<FileItem>(`/files/${fileId}/versions`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  restoreVersion: async (fileId: string, versionId: string): Promise<FileItem> => {
    const response = await axiosInstance.post<FileItem>(`/files/${fileId}/versions/${versionId}/restore`);
    return response.data;
  },

  downloadVersion: async (fileId: string, versionId: string, fileName: string, versionNum: number): Promise<void> => {
    const response = await axiosInstance.get(`/files/${fileId}/versions/${versionId}/download`, {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `v${versionNum}_${fileName}`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
