import { FileItem } from "./file.types";

export interface SearchParams {
  query?: string;
  extension?: string;
  uploadedAfter?: string;
  uploadedBefore?: string;
  folderId?: string;
  page?: number;
  size?: number;
}

export interface SearchResponse {
  results: FileItem[];
  totalResults: number;
  page: number;
  totalPages: number;
  queryEchoed: string | null;
}