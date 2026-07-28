export interface FileItem {
  id: string;
  originalName: string;
  mimeType: string;
  extension: string;
  sizeBytes: number;
  formattedSize: string;
  folderId: string | null;
  isTrashed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RenameFilePayload {
  newName: string;
}

export interface MoveFilePayload {
  targetFolderId: string | null;
}
