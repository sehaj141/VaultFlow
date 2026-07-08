import axiosInstance from "./axiosInstance";
import { DownloadUrlResponse, FileItem } from "../types/file.types";

export const filesApi = {
  list: (folderId: string | null) =>
    axiosInstance.get<FileItem[]>("/files", { params: { folderId } }),

  upload: (folderId: string | null, file: File, onProgress?: (pct: number) => void) => {
    const formData = new FormData();
    formData.append("file", file);
    return axiosInstance.post<FileItem>("/files", formData, {
      params: { folderId },
      headers: { "Content-Type": "multipart/form-data" },
      onUploadProgress: (e) => {
        if (onProgress && e.total) {
          onProgress(Math.round((e.loaded * 100) / e.total));
        }
      },
    });
  },

  getDownloadUrl: (fileId: string) =>
    axiosInstance.get<DownloadUrlResponse>(`/files/${fileId}/download`),

  rename: (fileId: string, name: string) =>
    axiosInstance.put<FileItem>(`/files/${fileId}/rename`, { name }),

  move: (fileId: string, newFolderId: string | null) =>
    axiosInstance.put<FileItem>(`/files/${fileId}/move`, { newFolderId }),

  remove: (fileId: string) => axiosInstance.delete(`/files/${fileId}`),
};