import { axiosInstance } from './axiosInstance';
import { FileItem } from '../types/file.types';
import { Folder } from '../types/folder.types';

export interface TrashResponseData {
  files: FileItem[];
  folders: Folder[];
}

export const trashApi = {
  getTrashedItems: async (): Promise<TrashResponseData> => {
    const response = await axiosInstance.get<TrashResponseData>('/trash');
    return response.data;
  },

  restoreFile: async (id: string): Promise<FileItem> => {
    const response = await axiosInstance.post<FileItem>(`/trash/files/${id}/restore`);
    return response.data;
  },

  restoreFolder: async (id: string): Promise<Folder> => {
    const response = await axiosInstance.post<Folder>(`/trash/folders/${id}/restore`);
    return response.data;
  },

  permanentlyDeleteFile: async (id: string): Promise<void> => {
    await axiosInstance.delete(`/trash/files/${id}`);
  },

  permanentlyDeleteFolder: async (id: string): Promise<void> => {
    await axiosInstance.delete(`/trash/folders/${id}`);
  },

  emptyTrashBin: async (): Promise<void> => {
    await axiosInstance.delete('/trash/empty');
  },
};
