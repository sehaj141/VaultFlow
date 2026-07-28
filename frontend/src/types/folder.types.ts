export interface Breadcrumb {
  id: string;
  name: string;
}

export interface Folder {
  id: string;
  name: string;
  parentId: string | null;
  path: string;
  depth: number;
  isTrashed: boolean;
  createdAt: string;
  updatedAt: string;
  breadcrumbs: Breadcrumb[];
  subfolderCount: number;
}

export interface CreateFolderPayload {
  name: string;
  parentId?: string | null;
}

export interface RenameFolderPayload {
  newName: string;
}

export interface MoveFolderPayload {
  targetParentId: string | null;
}
