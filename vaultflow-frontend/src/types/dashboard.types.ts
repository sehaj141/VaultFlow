export interface StorageUsage {
    usedBytes: number;
    limitBytes: number;
    percentageUsed: number;
  }
  
  export interface RecentItem {
    id: string;
    name: string;
    type: "file" | "folder";
    extension: string | null;
    sizeBytes: number | null;
    createdAt: string;
  }
  
  export interface FileTypeBreakdown {
    extension: string;
    count: number;
    totalSizeBytes: number;
  }
  
  export interface Dashboard {
    storageUsage: StorageUsage;
    totalFiles: number;
    totalFolders: number;
    recentUploads: RecentItem[];
    fileTypeBreakdown: FileTypeBreakdown[];
  }