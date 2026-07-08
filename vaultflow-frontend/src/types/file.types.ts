export interface FileItem {
    id: string;
    name: string;
    extension: string;
    mimeType: string;
    sizeBytes: number;
    folderId: string | null;
    createdAt: string;
    updatedAt: string;
  }
  
  export interface DownloadUrlResponse {
    url: string;
    expiresInSeconds: number;
  }