import { User } from './auth.types';

export interface FileVersionItem {
  id: string;
  fileId: string;
  versionNumber: number;
  sizeBytes: number;
  formattedSize: string;
  mimeType: string;
  uploadedBy: User;
  createdAt: string;
}
