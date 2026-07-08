import { FileItem } from "../types/file.types";
import { useFiles } from "../hooks/useFiles";

const EXTENSION_ICONS: Record<string, string> = {
  pdf: "📄", docx: "📝", txt: "📃", zip: "🗜️", png: "🖼️", jpg: "🖼️",
};

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

interface Props {
  files: FileItem[];
  folderId: string | null;
}

export default function FileGrid({ files, folderId }: Props) {
  const { deleteFile, downloadFile } = useFiles(folderId);

  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
      {files.map((file) => (
        <div
          key={file.id}
          className="group relative border border-slate-200 rounded-lg p-4 hover:shadow-md transition bg-white"
        >
          <div className="text-3xl mb-2">{EXTENSION_ICONS[file.extension] ?? "📄"}</div>
          <p className="text-sm font-medium text-slate-800 truncate">{file.name}</p>
          <p className="text-xs text-slate-400">{formatSize(file.sizeBytes)}</p>

          <div className="absolute top-2 right-2 hidden group-hover:flex gap-1">
            <button
              onClick={() => downloadFile(file.id, file.name)}
              className="text-xs bg-slate-100 px-1.5 py-0.5 rounded hover:bg-slate-200"
            >
              ⬇
            </button>
            <button
              onClick={() => deleteFile.mutate(file.id)}
              className="text-xs bg-red-50 text-red-600 px-1.5 py-0.5 rounded hover:bg-red-100"
            >
              🗑
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}