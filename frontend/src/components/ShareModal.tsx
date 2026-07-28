import React, { useState } from 'react';
import { Share2, X, Loader2, Copy, Check, Lock, Clock, Shield } from 'lucide-react';
import axios from 'axios';
import { shareApi } from '../api/shareApi';
import { PermissionRole, ShareLinkResponse } from '../types/share.types';

interface ShareModalProps {
  isOpen: boolean;
  onClose: () => void;
  fileId?: string | null;
  folderId?: string | null;
  resourceName: string;
}

export const ShareModal: React.FC<ShareModalProps> = ({
  isOpen,
  onClose,
  fileId,
  folderId,
  resourceName,
}) => {
  const [role, setRole] = useState<PermissionRole>('VIEWER');
  const [password, setPassword] = useState('');
  const [expirationHours, setExpirationHours] = useState<number | null>(null);
  const [generatedLink, setGeneratedLink] = useState<ShareLinkResponse | null>(null);
  const [copied, setCopied] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleGenerateLink = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const linkResponse = await shareApi.createShareLink({
        fileId,
        folderId,
        role,
        password: password.trim() || undefined,
        expirationHours,
      });

      setGeneratedLink(linkResponse);
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Failed to generate share link.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopyLink = () => {
    if (!generatedLink) return;
    const fullUrl = `${window.location.origin}${generatedLink.shareUrl}`;
    navigator.clipboard.writeText(fullUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
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
          <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Share2 className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-white">Share Resource</h3>
            <p className="text-xs text-slate-400">Sharing: <span className="text-indigo-300 font-medium">{resourceName}</span></p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-xs">
            {error}
          </div>
        )}

        {generatedLink ? (
          <div className="space-y-4 py-2">
            <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 space-y-3">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Shareable Link</span>
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  readOnly
                  value={`${window.location.origin}${generatedLink.shareUrl}`}
                  className="w-full px-3 py-2 rounded-lg glass-input text-xs font-mono select-all"
                />
                <button
                  onClick={handleCopyLink}
                  className="px-3 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5 transition shrink-0"
                >
                  {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                  {copied ? 'Copied' : 'Copy'}
                </button>
              </div>
            </div>

            <div className="text-xs text-slate-400 space-y-1.5 px-1">
              <p>• Access Role: <span className="text-indigo-400 font-semibold">{generatedLink.role}</span></p>
              {generatedLink.isPasswordProtected && (
                <p>• Password Protection: <span className="text-emerald-400 font-semibold">Enabled</span></p>
              )}
              {generatedLink.expiresAt && (
                <p>• Expires: <span className="text-amber-400 font-semibold">{new Date(generatedLink.expiresAt).toLocaleString()}</span></p>
              )}
            </div>

            <button
              onClick={() => setGeneratedLink(null)}
              className="w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition"
            >
              Configure Different Link
            </button>
          </div>
        ) : (
          <form onSubmit={handleGenerateLink} className="space-y-4">
            {/* Role Selection */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Permission Role (RBAC)
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setRole('VIEWER')}
                  className={`p-3 rounded-xl border text-left transition text-xs ${
                    role === 'VIEWER'
                      ? 'bg-indigo-600/20 border-indigo-500 text-white font-semibold'
                      : 'bg-slate-900/40 border-slate-800 text-slate-400 hover:bg-slate-800'
                  }`}
                >
                  <Shield className="w-4 h-4 text-indigo-400 mb-1" />
                  <div>Viewer (Read-Only)</div>
                </button>

                <button
                  type="button"
                  onClick={() => setRole('EDITOR')}
                  className={`p-3 rounded-xl border text-left transition text-xs ${
                    role === 'EDITOR'
                      ? 'bg-indigo-600/20 border-indigo-500 text-white font-semibold'
                      : 'bg-slate-900/40 border-slate-800 text-slate-400 hover:bg-slate-800'
                  }`}
                >
                  <Shield className="w-4 h-4 text-emerald-400 mb-1" />
                  <div>Editor (Upload/Edit)</div>
                </button>
              </div>
            </div>

            {/* Optional Password Protection */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Lock className="w-3.5 h-3.5 text-amber-400" />
                Password Protection (Optional)
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Leave blank for public link"
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            {/* Expiration Selector */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-cyan-400" />
                Expiration Window
              </label>
              <select
                value={expirationHours ?? ''}
                onChange={(e) => setExpirationHours(e.target.value ? Number(e.target.value) : null)}
                className="w-full px-4 py-2.5 rounded-xl glass-input text-xs focus:ring-2 focus:ring-indigo-500"
              >
                <option value="" className="bg-slate-900 text-slate-100">Never Expires</option>
                <option value="24" className="bg-slate-900 text-slate-100">24 Hours</option>
                <option value="168" className="bg-slate-900 text-slate-100">7 Days</option>
                <option value="720" className="bg-slate-900 text-slate-100">30 Days</option>
              </select>
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
                disabled={isLoading}
                className="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-semibold shadow-lg shadow-indigo-600/30 flex items-center gap-2 transition disabled:opacity-50"
              >
                {isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Generate Share Link'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
