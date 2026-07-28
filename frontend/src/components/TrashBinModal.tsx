import React, { useState, useEffect, useCallback } from 'react';
import { Trash2, X, Loader2, RotateCcw, AlertTriangle, FileText, Folder as FolderIcon } from 'lucide-react';
import { trashApi, TrashResponseData } from '../api/trashApi';

interface TrashBinModalProps {
  isOpen: boolean;
  onClose: () => void;
  onItemRestored: () => void;
}

export const TrashBinModal: React.FC<TrashBinModalProps> = ({
  isOpen,
  onClose,
  onItemRestored,
}) => {
  const [trashData, setTrashData] = useState<TrashResponseData>({ files: [], folders: [] });
  const [isLoading, setIsLoading] = useState(false);

  const fetchTrash = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await trashApi.getTrashedItems();
      setTrashData(data);
    } catch (err) {
      console.error('Failed to fetch trash items:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      fetchTrash();
    }
  }, [isOpen, fetchTrash]);

  if (!isOpen) return null;

  const handleRestoreFile = async (id: string) => {
    await trashApi.restoreFile(id);
    await fetchTrash();
    onItemRestored();
  };

  const handleRestoreFolder = async (id: string) => {
    await trashApi.restoreFolder(id);
    await fetchTrash();
    onItemRestored();
  };

  const handlePermanentDeleteFile = async (id: string) => {
    if (window.confirm('Permanently delete this file? This action CANNOT be undone.')) {
      await trashApi.permanentlyDeleteFile(id);
      await fetchTrash();
    }
  };

  const handlePermanentDeleteFolder = async (id: string) => {
    if (window.confirm('Permanently delete this folder and all contents? This action CANNOT be undone.')) {
      await trashApi.permanentlyDeleteFolder(id);
      await fetchTrash();
    }
  };

  const handleEmptyTrash = async () => {
    if (window.confirm('Empty entire Trash Bin? ALL trashed items will be permanently erased.')) {
      await trashApi.emptyTrashBin();
      await fetchTrash();
      onItemRestored();
    }
  };

  const totalItems = trashData.files.length + trashData.folders.length;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
      <div className="w-full max-w-lg glass-card rounded-2xl p-6 shadow-2xl relative border border-slate-800 flex flex-col max-h-[85vh]">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center justify-between gap-3 mb-6 shrink-0 pr-8">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-xl bg-red-500/10 text-red-400 border border-red-500/20">
              <Trash2 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">Trash Bin Recovery</h3>
              <p className="text-xs text-slate-400">Items auto-delete permanently after 30 days</p>
            </div>
          </div>

          {totalItems > 0 && (
            <button
              onClick={handleEmptyTrash}
              className="px-3 py-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 text-xs font-semibold border border-red-500/20 transition shrink-0"
            >
              Empty Bin
            </button>
          )}
        </div>

        {/* List Body */}
        <div className="flex-1 overflow-y-auto pr-1 space-y-3">
          {isLoading ? (
            <div className="py-12 flex flex-col items-center justify-center text-slate-400 gap-2">
              <Loader2 className="w-6 h-6 animate-spin text-red-500" />
              <p className="text-xs">Scanning trash bin...</p>
            </div>
          ) : totalItems === 0 ? (
            <div className="py-12 text-center text-xs text-slate-500 space-y-2">
              <Trash2 className="w-8 h-8 mx-auto text-slate-700" />
              <p>Your Trash Bin is clean &amp; empty.</p>
            </div>
          ) : (
            <>
              {/* Folders */}
              {trashData.folders.map((folder) => (
                <div
                  key={folder.id}
                  className="p-3.5 rounded-xl bg-slate-900/50 border border-slate-800 flex items-center justify-between text-xs"
                >
                  <div className="flex items-center gap-3 truncate">
                    <div className="p-2 rounded-lg bg-amber-500/10 text-amber-400 border border-amber-500/20 shrink-0">
                      <FolderIcon className="w-4 h-4" />
                    </div>
                    <div className="truncate">
                      <h4 className="font-bold text-slate-200 truncate">{folder.name}</h4>
                      <span className="text-[10px] text-amber-400">Folder</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <button
                      onClick={() => handleRestoreFolder(folder.id)}
                      className="px-2.5 py-1 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 font-semibold flex items-center gap-1 transition"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      Restore
                    </button>
                    <button
                      onClick={() => handlePermanentDeleteFolder(folder.id)}
                      className="p-1.5 rounded-lg text-slate-500 hover:text-red-400 hover:bg-red-500/10 transition"
                      title="Delete Permanently"
                    >
                      <AlertTriangle className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}

              {/* Files */}
              {trashData.files.map((file) => (
                <div
                  key={file.id}
                  className="p-3.5 rounded-xl bg-slate-900/50 border border-slate-800 flex items-center justify-between text-xs"
                >
                  <div className="flex items-center gap-3 truncate">
                    <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 shrink-0">
                      <FileText className="w-4 h-4" />
                    </div>
                    <div className="truncate">
                      <h4 className="font-bold text-slate-200 truncate">{file.originalName}</h4>
                      <span className="text-[10px] text-slate-400 font-mono">{file.formattedSize}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <button
                      onClick={() => handleRestoreFile(file.id)}
                      className="px-2.5 py-1 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 font-semibold flex items-center gap-1 transition"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      Restore
                    </button>
                    <button
                      onClick={() => handlePermanentDeleteFile(file.id)}
                      className="p-1.5 rounded-lg text-slate-500 hover:text-red-400 hover:bg-red-500/10 transition"
                      title="Delete Permanently"
                    >
                      <AlertTriangle className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </>
          )}
        </div>
      </div>
    </div>
  );
};
