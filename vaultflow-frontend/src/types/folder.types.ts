export interface Folder {
    id: string;
    name: string;
    parentId: string | null;
    hasChildren: boolean;
    createdAt: string;
    updatedAt: string;
  }
  
  export interface Breadcrumb {
    id: string;
    name: string;
  }
  
  export interface CreateFolderRequest {
    name: string;
    parentId: string | null;
  }