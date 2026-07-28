import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { shareApi } from '../api/shareApi';
import { PublicSharedResource } from '../types/share.types';
import {
  HardDrive,
  Lock,
  Download,
  FileText,
  Folder as FolderIcon,
  Shield,
  Loader2,
  AlertCircle,
  ArrowRight,
  FileCode,
  FileArchive,
  Image as ImageIcon,
} from 'lucide-react';

export const PublicSharePage: React.FC = () => {
  const { token } = useParams<{ token: string }>();

  const [resource, setResource] = useState<PublicSharedResource | null>(null);
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isVerifying, setIsVerifying] = useState(false);

  const fetchResource = useCallback(async () => {
    if (!token) return;
    setIsLoading(true);
    try {
      const data = await shareApi.getPublicSharedResource(token);
      setResource(data);
    } catch (err: unknown) {
      setError('This share link is invalid, expired, or has been revoked.');
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    fetchResource();
  }, [fetchResource]);

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return;

    setError(null);
    setIsVerifying(true);

    try {
      const data = await shareApi.verifyPasswordAndAccess(token, password);
      setResource(data);
    } catch {
      setError('Incorrect password. Access denied.');
    } finally {
      setIsVerifying(false);
    }
  };

  const handleDownload = async () => {
    if (!token || !resource) return;
    await shareApi.downloadSharedFile(token, password, resource.resourceName);
  };

  const getFileIcon = (ext?: string) => {
    if (!ext) return <FileText className="w-8 h-8 text-slate-400" />;
    const cleanExt = ext.toLowerCase();
    if (['png', 'jpg', 'jpeg', 'svg'].includes(cleanExt)) {
      return <ImageIcon className="w-8 h-8 text-purple-400" />;
    }
    if (cleanExt === 'pdf') {
      return <FileText className="w-8 h-8 text-red-400" />;
    }
    if (cleanExt === 'docx' || cleanExt === 'txt') {
      return <FileCode className="w-8 h-8 text-blue-400" />;
    }
    if (cleanExt === 'zip') {
      return <FileArchive className="w-8 h-8 text-amber-400" />;
    }
    return <FileText className="w-8 h-8 text-slate-400" />;
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between p-6 relative overflow-hidden">
      <div className="absolute top-1/4 left-1/3 w-96 h-96 bg-indigo-600/15 rounded-full blur-3xl pointer-events-none" />

      {/* Header */}
      <header className="flex items-center justify-between max-w-5xl mx-auto w-full glass-card p-4 rounded-2xl mb-8">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
            <HardDrive className="w-6 h-6" />
          </div>
          <div>
            <h1 className="font-bold text-lg text-white">VaultFlow Shared Portal</h1>
            <p className="text-xs text-slate-400">Secure Cloud Link Sharing</p>
          </div>
        </div>

        {resource && (
          <div className="flex items-center gap-2 px-3 py-1 rounded-lg bg-indigo-500/10 border border-indigo-500/20 text-xs text-indigo-400 font-semibold">
            <Shield className="w-3.5 h-3.5" />
            Role: {resource.permissionRole}
          </div>
        )}
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-xl mx-auto w-full flex items-center justify-center">
        {isLoading ? (
          <div className="flex flex-col items-center gap-3 text-slate-400">
            <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
            <p className="text-sm font-medium">Verifying share link security token...</p>
          </div>
        ) : error && !resource ? (
          <div className="glass-card rounded-2xl p-8 text-center border border-red-500/20 max-w-md w-full">
            <div className="w-14 h-14 rounded-2xl bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-400 mx-auto mb-4">
              <AlertCircle className="w-8 h-8" />
            </div>
            <h2 className="text-xl font-bold text-white mb-2">Access Restricted</h2>
            <p className="text-sm text-slate-400 leading-relaxed">{error}</p>
          </div>
        ) : resource && resource.isPasswordProtected && !resource.isPasswordVerified ? (
          /* Password Form */
          <div className="glass-card rounded-2xl p-8 shadow-2xl w-full border border-slate-800">
            <div className="text-center mb-6">
              <div className="w-14 h-14 rounded-2xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400 mx-auto mb-3">
                <Lock className="w-8 h-8" />
              </div>
              <h2 className="text-xl font-bold text-white">Password Protected Link</h2>
              <p className="text-xs text-slate-400 mt-1">Enter the password provided by the owner to unlock access</p>
            </div>

            {error && (
              <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-xs">
                {error}
              </div>
            )}

            <form onSubmit={handlePasswordSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-2">
                  Link Password
                </label>
                <input
                  type="password"
                  required
                  autoFocus
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-4 py-3 rounded-xl glass-input text-sm focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <button
                type="submit"
                disabled={isVerifying || !password}
                className="w-full py-3 px-4 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-semibold text-sm shadow-lg shadow-amber-600/30 flex items-center justify-center gap-2 transition disabled:opacity-50"
              >
                {isVerifying ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Authenticating Password...
                  </>
                ) : (
                  <>
                    Unlock Resource
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </form>
          </div>
        ) : (
          /* Unlocked Resource View */
          <div className="glass-card rounded-2xl p-8 shadow-2xl w-full border border-slate-800 text-center">
            <div className="w-16 h-16 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-center mx-auto mb-4">
              {resource?.resourceType === 'FILE' ? (
                getFileIcon(resource.fileDetails?.extension)
              ) : (
                <FolderIcon className="w-8 h-8 text-amber-400" />
              )}
            </div>

            <h2 className="text-2xl font-bold text-white truncate mb-1">{resource?.resourceName}</h2>
            <p className="text-xs text-slate-400 mb-6">
              Shared as <span className="text-indigo-300 font-semibold">{resource?.permissionRole}</span> permission
            </p>

            {resource?.resourceType === 'FILE' && resource.fileDetails && (
              <div className="space-y-6">
                <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 text-left text-xs space-y-2">
                  <div className="flex justify-between">
                    <span className="text-slate-400">Size:</span>
                    <span className="font-semibold text-slate-200">{resource.fileDetails.formattedSize}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-400">Extension:</span>
                    <span className="font-mono text-indigo-400 uppercase">{resource.fileDetails.extension}</span>
                  </div>
                </div>

                <button
                  onClick={handleDownload}
                  className="w-full py-3.5 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm shadow-lg shadow-indigo-600/30 flex items-center justify-center gap-2 transition"
                >
                  <Download className="w-4 h-4" />
                  Download File Binary
                </button>
              </div>
            )}

            {resource?.resourceType === 'FOLDER' && resource.folderFiles && (
              <div className="text-left space-y-3">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  Shared Directory Files ({resource.folderFiles.length})
                </h3>
                <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
                  {resource.folderFiles.map((file) => (
                    <div key={file.id} className="p-3 rounded-xl bg-slate-900/50 border border-slate-800 flex items-center justify-between">
                      <span className="text-sm text-slate-200 truncate">{file.originalName}</span>
                      <span className="text-xs font-mono text-slate-400">{file.formattedSize}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="text-center text-xs text-slate-500 py-4">
        Powered by VaultFlow Cloud Platform &bull; End-to-End Encryption
      </footer>
    </div>
  );
};
