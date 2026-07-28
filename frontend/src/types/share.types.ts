import { FileItem } from './file.types';
import { Folder } from './folder.types';

export type PermissionRole = 'OWNER' | 'EDITOR' | 'VIEWER';

export interface CreateShareLinkPayload {
  fileId?: string | null;
  folderId?: string | null;
  role: PermissionRole;
  password?: string;
  expirationHours?: number | null;
}

export interface ShareLinkResponse {
  id: string;
  token: string;
  shareUrl: string;
  role: PermissionRole;
  isPasswordProtected: boolean;
  expiresAt: string | null;
  accessCount: number;
  isActive: boolean;
  createdAt: string;
}

export interface PublicSharedResource {
  token: string;
  resourceType: 'FILE' | 'FOLDER';
  resourceName: string;
  permissionRole: PermissionRole;
  isPasswordProtected: boolean;
  isPasswordVerified: boolean;
  fileDetails?: FileItem;
  folderDetails?: Folder;
  folderFiles?: FileItem[];
  folderSubfolders?: Folder[];
}
