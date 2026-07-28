import { FileItem } from './file.types';

export interface ParsedSearchFilter {
  query?: string | null;
  extension?: string | null;
  minSizeBytes?: number | null;
  maxSizeBytes?: number | null;
  daysAgo?: number | null;
  interpretationSummary: string;
}

export interface AiSearchResponse {
  originalPrompt: string;
  parsedFilter: ParsedSearchFilter;
  matchingFiles: FileItem[];
}
