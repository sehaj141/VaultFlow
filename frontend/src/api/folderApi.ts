import { axiosInstance } from './axiosInstance';
import { CreateFolderPayload, Folder, MoveFolderPayload, RenameFolderPayload } from '../types/folder.types';

export const folderApi = {
  createFolder: async (payload: CreateFolderPayload): Promise<Folder> => {
    const response = await axiosInstance.post<Folder>('/folders', payload);
    return response.data;
  },

  getFolderById: async (id: string): Promise<Folder> => {
    const response = await axiosInstance.get<Folder>(`/folders/${id}`);
    return response.data;
  },

  getSubfolders: async (parentId?: string | null): Promise<Folder[]> => {
    const params = parentId ? { parentId } : {};
    const response = await axiosInstance.get<Folder[]>('/folders', { params });
    return response.data;
  },

  renameFolder: async (id: string, payload: RenameFolderPayload): Promise<Folder> => {
    const response = await axiosInstance.patch<Folder>(`/folders/${id}/rename`, payload);
    return response.data;
  },

  moveFolder: async (id: string, payload: MoveFolderPayload): Promise<Folder> => {
    const response = await axiosInstance.put<Folder>(`/folders/${id}/move`, payload);
    return response.data;
  },

  deleteFolder: async (id: string): Promise<void> => {
    await axiosInstance.delete(`/folders/${id}`);
  },
};
