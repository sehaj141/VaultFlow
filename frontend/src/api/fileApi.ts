import { axiosInstance } from './axiosInstance';
import { FileItem, MoveFilePayload, RenameFilePayload } from '../types/file.types';

export interface SearchParams {
  query?: string;
  extension?: string;
  folderId?: string | null;
}

export const fileApi = {
  uploadFile: async (file: File, folderId?: string | null): Promise<FileItem> => {
    const formData = new FormData();
    formData.append('file', file);
    if (folderId) {
      formData.append('folderId', folderId);
    }

    const response = await axiosInstance.post<FileItem>('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getFiles: async (folderId?: string | null): Promise<FileItem[]> => {
    const params = folderId ? { folderId } : {};
    const response = await axiosInstance.get<FileItem[]>('/files', { params });
    return response.data;
  },

  searchFiles: async (searchParams: SearchParams): Promise<FileItem[]> => {
    const params: Record<string, string> = {};
    if (searchParams.query) params.query = searchParams.query;
    if (searchParams.extension) params.extension = searchParams.extension;
    if (searchParams.folderId) params.folderId = searchParams.folderId;

    const response = await axiosInstance.get<FileItem[]>('/files/search', { params });
    return response.data;
  },

  getFileBlobUrl: async (id: string, mimeType?: string): Promise<string> => {
    const response = await axiosInstance.get(`/files/${id}/download`, {
      responseType: 'blob',
    });
    const blob = new Blob([response.data], { type: mimeType || 'application/pdf' });
    return window.URL.createObjectURL(blob);
  },

  getFileTextContent: async (id: string): Promise<string> => {
    const response = await axiosInstance.get(`/files/${id}/download`, {
      responseType: 'text',
      transformResponse: [(data) => data],
    });
    return response.data;
  },

  downloadFile: async (id: string, fileName: string): Promise<void> => {
    const response = await axiosInstance.get(`/files/${id}/download`, {
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  renameFile: async (id: string, payload: RenameFilePayload): Promise<FileItem> => {
    const response = await axiosInstance.patch<FileItem>(`/files/${id}/rename`, payload);
    return response.data;
  },

  moveFile: async (id: string, payload: MoveFilePayload): Promise<FileItem> => {
    const response = await axiosInstance.put<FileItem>(`/files/${id}/move`, payload);
    return response.data;
  },

  deleteFile: async (id: string): Promise<void> => {
    await axiosInstance.delete(`/files/${id}`);
  },
};
