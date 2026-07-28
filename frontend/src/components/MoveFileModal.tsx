import React, { useState, useEffect } from 'react';
import { FolderInput, X, Loader2, Folder as FolderIcon, HardDrive } from 'lucide-react';
import axios from 'axios';
import { FileItem } from '../types/file.types';
import { Folder } from '../types/folder.types';
import { folderApi } from '../api/folderApi';

interface MoveFileModalProps {
  file: FileItem | null;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (fileId: string, targetFolderId: string | null) => Promise<void>;
}

export const MoveFileModal: React.FC<MoveFileModalProps> = ({
  file,
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [availableFolders, setAvailableFolders] = useState<Folder[]>([]);
  const [selectedFolderId, setSelectedFolderId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen && file) {
      const fetchAvailableFolders = async () => {
        setIsLoading(true);
        try {
          const rootFolders = await folderApi.getSubfolders(null);
          setAvailableFolders(rootFolders);
          setSelectedFolderId(file.folderId);
        } catch {
          setError('Failed to load folders.');
        } finally {
          setIsLoading(false);
        }
      };

      fetchAvailableFolders();
    }
  }, [isOpen, file]);

  if (!isOpen || !file) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await onSubmit(file.id, selectedFolderId);
      onClose();
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Failed to move file.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
      <div className="w-full max-w-md glass-card rounded-2xl p-6 shadow-2xl relative border border-slate-800">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-4">
          <div className="p-3 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            <FolderInput className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-white">Move File</h3>
            <p className="text-xs text-slate-400">File: <span className="text-cyan-300 font-medium">{file.originalName}</span></p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-xs">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Select Destination Directory
            </label>

            {isLoading ? (
              <div className="py-6 flex justify-center text-slate-400">
                <Loader2 className="w-5 h-5 animate-spin" />
              </div>
            ) : (
              <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                <button
                  type="button"
                  onClick={() => setSelectedFolderId(null)}
                  className={`w-full p-3 rounded-xl border flex items-center gap-3 transition text-left text-sm ${
                    selectedFolderId === null
                      ? 'bg-cyan-600/20 border-cyan-500 text-white font-semibold'
                      : 'bg-slate-900/40 border-slate-800 text-slate-300 hover:bg-slate-800/60'
                  }`}
                >
                  <HardDrive className="w-4 h-4 text-indigo-400" />
                  <span>/ (Root Storage)</span>
                </button>

                {availableFolders.map((folder) => (
                  <button
                    key={folder.id}
                    type="button"
                    onClick={() => setSelectedFolderId(folder.id)}
                    className={`w-full p-3 rounded-xl border flex items-center gap-3 transition text-left text-sm ${
                      selectedFolderId === folder.id
                        ? 'bg-cyan-600/20 border-cyan-500 text-white font-semibold'
                        : 'bg-slate-900/40 border-slate-800 text-slate-300 hover:bg-slate-800/60'
                    }`}
                  >
                    <FolderIcon className="w-4 h-4 text-amber-400" />
                    <span className="truncate">{folder.name}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl text-sm font-medium text-slate-300 hover:bg-slate-800 transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || isLoading}
              className="px-5 py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-sm font-semibold shadow-lg shadow-cyan-600/30 flex items-center gap-2 transition disabled:opacity-50"
            >
              {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Move File'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
