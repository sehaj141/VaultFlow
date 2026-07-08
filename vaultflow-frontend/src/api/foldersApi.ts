import axiosInstance from "./axiosInstance";
import { Breadcrumb, CreateFolderRequest, Folder } from "../types/folder.types";

export const foldersApi = {
  list: (parentId: string | null) =>
    axiosInstance.get<Folder[]>("/folders", { params: { parentId } }),

  create: (data: CreateFolderRequest) =>
    axiosInstance.post<Folder>("/folders", data),

  rename: (folderId: string, name: string) =>
    axiosInstance.put<Folder>(`/folders/${folderId}/rename`, { name }),

  move: (folderId: string, newParentId: string | null) =>
    axiosInstance.put<Folder>(`/folders/${folderId}/move`, { newParentId }),

  remove: (folderId: string) =>
    axiosInstance.delete(`/folders/${folderId}`),

  breadcrumb: (folderId: string) =>
    axiosInstance.get<Breadcrumb[]>(`/folders/${folderId}/breadcrumb`),
};