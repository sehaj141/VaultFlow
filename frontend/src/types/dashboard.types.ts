import { FileItem } from './file.types';
import { Folder } from './folder.types';

export interface FileTypeCategoryStats {
  categoryName: string;
  fileCount: number;
  sizeBytes: number;
  formattedSize: string;
  percentageOfTotalStorage: number;
}

export interface DashboardAnalytics {
  usedStorageBytes: number;
  maxStorageBytes: number;
  usagePercentage: number;
  formattedUsedStorage: string;
  formattedMaxStorage: string;
  totalFilesCount: number;
  totalFoldersCount: number;
  categoryBreakdown: FileTypeCategoryStats[];
  recentUploads: FileItem[];
  recentFolders: Folder[];
}
