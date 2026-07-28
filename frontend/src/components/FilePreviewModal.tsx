import React, { useEffect, useState } from 'react';
import { X, Download, Loader2, FileText, Eye, FileCode } from 'lucide-react';
import { fileApi } from '../api/fileApi';
import { FileItem } from '../types/file.types';

interface FilePreviewModalProps {
  file: FileItem | null;
  isOpen: boolean;
  onClose: () => void;
}

export const FilePreviewModal: React.FC<FilePreviewModalProps> = ({ file, isOpen, onClose }) => {
  const [blobUrl, setBlobUrl] = useState<string | null>(null);
  const [textContent, setTextContent] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const cleanExt = file?.extension.toLowerCase() || '';
  const isPdf = cleanExt === 'pdf';
  const isImage = ['png', 'jpg', 'jpeg', 'svg', 'webp', 'gif'].includes(cleanExt);
  const isTextDocument = [
    'txt',
    'md',
    'json',
    'csv',
    'java',
    'py',
    'js',
    'ts',
    'tsx',
    'jsx',
    'html',
    'css',
    'xml',
    'log',
    'yml',
    'yaml',
    'env',
  ].includes(cleanExt);

  useEffect(() => {
    if (!file || !isOpen) return;

    let activeUrl: string | null = null;
    setIsLoading(true);
    setError(null);
    setTextContent(null);
    setBlobUrl(null);

    if (isTextDocument) {
      fileApi
        .getFileTextContent(file.id)
        .then((text) => {
          setTextContent(text);
        })
        .catch((err) => {
          console.error('Failed to load text document:', err);
          setError('Failed to read document content.');
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      fileApi
        .getFileBlobUrl(file.id, file.mimeType)
        .then((url) => {
          activeUrl = url;
          setBlobUrl(url);
        })
        .catch((err) => {
          console.error('Failed to load file preview:', err);
          setError('Failed to render preview. Download the file to view.');
        })
        .finally(() => {
          setIsLoading(false);
        });
    }

    return () => {
      if (activeUrl) {
        window.URL.revokeObjectURL(activeUrl);
      }
    };
  }, [file, isOpen, isTextDocument]);

  if (!isOpen || !file) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/85 backdrop-blur-md">
      <div className="w-full max-w-5xl glass-card rounded-2xl p-6 shadow-2xl relative flex flex-col h-[88vh] border border-indigo-500/30">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800 shrink-0">
          <div className="flex items-center gap-3 truncate pr-4">
            <div className="p-2.5 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              {isTextDocument ? <FileCode className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
            </div>
            <div className="truncate">
              <h3 className="text-base font-bold text-white truncate">{file.originalName}</h3>
              <p className="text-xs text-slate-400 font-mono">
                {file.formattedSize} • {file.extension.toUpperCase()}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={() => fileApi.downloadFile(file.id, file.originalName)}
              className="px-3.5 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-600/20 flex items-center gap-2 transition"
            >
              <Download className="w-4 h-4" />
              Download
            </button>
            <button
              onClick={onClose}
              className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Content Viewer Body */}
        <div className="flex-1 overflow-hidden relative mt-4 rounded-xl bg-slate-900/80 border border-slate-800 flex items-center justify-center">
          {isLoading ? (
            <div className="flex flex-col items-center gap-3 text-slate-400">
              <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
              <p className="text-sm font-mono">Loading Document Preview...</p>
            </div>
          ) : error ? (
            <div className="p-8 text-center max-w-md">
              <FileText className="w-12 h-12 text-slate-500 mx-auto mb-3" />
              <p className="text-sm text-slate-300 mb-4">{error}</p>
              <button
                onClick={() => fileApi.downloadFile(file.id, file.originalName)}
                className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold"
              >
                Download File
              </button>
            </div>
          ) : isTextDocument && textContent !== null ? (
            <div className="w-full h-full p-6 overflow-auto text-left font-mono text-xs text-slate-200 bg-slate-950/60 leading-relaxed whitespace-pre-wrap selection:bg-indigo-500 selection:text-white">
              {textContent}
            </div>
          ) : blobUrl ? (
            isPdf ? (
              <iframe
                src={blobUrl}
                title={file.originalName}
                className="w-full h-full rounded-xl border-0"
              />
            ) : isImage ? (
              <div className="w-full h-full flex items-center justify-center p-4">
                <img
                  src={blobUrl}
                  alt={file.originalName}
                  className="max-w-full max-h-full object-contain rounded-lg shadow-lg"
                />
              </div>
            ) : (
              <div className="p-8 text-center max-w-md">
                <FileText className="w-12 h-12 text-indigo-400 mx-auto mb-3" />
                <h4 className="text-base font-bold text-white mb-2">Binary Document Format</h4>
                <p className="text-xs text-slate-400 mb-6">
                  {file.extension.toUpperCase()} documents contain binary markup. Download to open in Word or your desktop editor.
                </p>
                <button
                  onClick={() => fileApi.downloadFile(file.id, file.originalName)}
                  className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-600/30 flex items-center gap-2 mx-auto"
                >
                  <Download className="w-4 h-4" />
                  Download {file.originalName}
                </button>
              </div>
            )
          ) : null}
        </div>
      </div>
    </div>
  );
};
