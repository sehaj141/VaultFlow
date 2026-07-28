import React, { useState, useEffect, useCallback } from 'react';
import { History, X, Loader2, Download, RotateCcw, UploadCloud, Clock, UserCheck } from 'lucide-react';
import { versionApi } from '../api/versionApi';
import { FileItem } from '../types/file.types';
import { FileVersionItem } from '../types/version.types';

interface VersionHistoryModalProps {
  file: FileItem | null;
  isOpen: boolean;
  onClose: () => void;
  onVersionRestored: () => void;
}

export const VersionHistoryModal: React.FC<VersionHistoryModalProps> = ({
  file,
  isOpen,
  onClose,
  onVersionRestored,
}) => {
  const [versions, setVersions] = useState<FileVersionItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  const fetchVersions = useCallback(async () => {
    if (!file) return;
    setIsLoading(true);
    try {
      const timeline = await versionApi.getVersionTimeline(file.id);
      setVersions(timeline);
    } catch (error) {
      console.error('Failed to load version history:', error);
    } finally {
      setIsLoading(false);
    }
  }, [file]);

  useEffect(() => {
    if (isOpen) {
      fetchVersions();
    }
  }, [isOpen, fetchVersions]);

  if (!isOpen || !file) return null;

  const handleUploadNewVersion = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const newVersionFile = e.target.files[0];
      setIsUploading(true);
      try {
        await versionApi.uploadNewVersion(file.id, newVersionFile);
        await fetchVersions();
        onVersionRestored();
      } catch (err) {
        console.error('Failed to upload new version:', err);
      } finally {
        setIsUploading(false);
      }
    }
  };

  const handleRestore = async (versionId: string) => {
    if (window.confirm('Restore this historical version? Current file pointers will be updated.')) {
      try {
        await versionApi.restoreVersion(file.id, versionId);
        await fetchVersions();
        onVersionRestored();
      } catch (err) {
        console.error('Failed to restore version:', err);
      }
    }
  };

  const handleDownload = async (version: FileVersionItem) => {
    await versionApi.downloadVersion(file.id, version.id, file.originalName, version.versionNumber);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
      <div className="w-full max-w-lg glass-card rounded-2xl p-6 shadow-2xl relative border border-slate-800 flex flex-col max-h-[85vh]">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-6 shrink-0">
          <div className="p-3 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
            <History className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-white">Version Timeline</h3>
            <p className="text-xs text-slate-400">File: <span className="text-cyan-300 font-medium">{file.originalName}</span></p>
          </div>
        </div>

        {/* Action Header: Upload New Version */}
        <div className="mb-4 shrink-0">
          <input
            type="file"
            id="version-upload-input"
            onChange={handleUploadNewVersion}
            className="hidden"
          />
          <label
            htmlFor="version-upload-input"
            className="w-full p-3 rounded-xl bg-indigo-600/20 hover:bg-indigo-600/30 border border-indigo-500/30 text-indigo-300 text-xs font-semibold flex items-center justify-center gap-2 cursor-pointer transition"
          >
            {isUploading ? <Loader2 className="w-4 h-4 animate-spin" /> : <UploadCloud className="w-4 h-4" />}
            <span>Upload New Version (v{versions.length > 0 ? versions[0].versionNumber + 1 : 2})</span>
          </label>
        </div>

        {/* Timeline List */}
        <div className="flex-1 overflow-y-auto pr-1 space-y-3">
          {isLoading ? (
            <div className="py-12 flex flex-col items-center justify-center text-slate-400 gap-2">
              <Loader2 className="w-6 h-6 animate-spin text-indigo-500" />
              <p className="text-xs">Fetching version logs...</p>
            </div>
          ) : versions.length === 0 ? (
            <div className="py-8 text-center text-xs text-slate-500">
              No version history records found.
            </div>
          ) : (
            versions.map((v, idx) => (
              <div
                key={v.id}
                className={`p-4 rounded-xl border flex items-center justify-between transition ${
                  idx === 0
                    ? 'bg-indigo-950/40 border-indigo-500/40'
                    : 'bg-slate-900/40 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center gap-3.5 truncate">
                  <span className={`px-2.5 py-1 rounded-lg text-xs font-mono font-bold shrink-0 ${
                    idx === 0 ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-300'
                  }`}>
                    v{v.versionNumber} {idx === 0 ? '(Current)' : ''}
                  </span>

                  <div className="truncate text-xs space-y-1">
                    <div className="flex items-center gap-2 text-slate-300 font-medium truncate">
                      <Clock className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                      <span>{new Date(v.createdAt).toLocaleString()}</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-400">
                      <UserCheck className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                      <span>{v.uploadedBy.fullName}</span>
                      <span>&bull;</span>
                      <span className="font-mono text-indigo-300">{v.formattedSize}</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() => handleDownload(v)}
                    title="Download this version"
                    className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
                  >
                    <Download className="w-4 h-4 text-emerald-400" />
                  </button>

                  {idx !== 0 && (
                    <button
                      onClick={() => handleRestore(v.id)}
                      title="Restore to this version"
                      className="px-3 py-1.5 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 text-xs font-semibold border border-amber-500/20 flex items-center gap-1 transition"
                    >
                      <RotateCcw className="w-3.5 h-3.5" />
                      Restore
                    </button>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
