import { useState } from "react";
import { Folder } from "../types/folder.types";
import { useFolders } from "../hooks/useFolders";

interface Props {
  folders: Folder[];
  parentId: string | null;
  onOpen: (folderId: string) => void;
}

export default function FolderGrid({ folders, parentId, onOpen }: Props) {
  const { renameFolder, deleteFolder } = useFolders(parentId);
  const [renamingId, setRenamingId] = useState<string | null>(null);
  const [renameValue, setRenameValue] = useState("");

  const handleRenameSubmit = (id: string) => {
    if (renameValue.trim()) {
      renameFolder.mutate({ id, name: renameValue.trim() });
    }
    setRenamingId(null);
  };

  return (
    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
      {folders.map((folder) => (
        <div
          key={folder.id}
          className="group relative border border-slate-200 rounded-lg p-4 hover:shadow-md transition cursor-pointer bg-white"
          onDoubleClick={() => onOpen(folder.id)}
        >
          <div className="text-3xl mb-2">📁</div>

          {renamingId === folder.id ? (
            <input
              autoFocus
              value={renameValue}
              onChange={(e) => setRenameValue(e.target.value)}
              onBlur={() => handleRenameSubmit(folder.id)}
              onKeyDown={(e) => e.key === "Enter" && handleRenameSubmit(folder.id)}
              className="w-full border border-indigo-400 rounded px-1 text-sm"
            />
          ) : (
            <p className="text-sm font-medium text-slate-800 truncate">{folder.name}</p>
          )}

          <div className="absolute top-2 right-2 hidden group-hover:flex gap-1">
            <button
              onClick={() => {
                setRenamingId(folder.id);
                setRenameValue(folder.name);
              }}
              className="text-xs bg-slate-100 px-1.5 py-0.5 rounded hover:bg-slate-200"
            >
              ✎
            </button>
            <button
              onClick={() => deleteFolder.mutate(folder.id)}
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