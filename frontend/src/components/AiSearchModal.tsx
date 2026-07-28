import React, { useState } from 'react';
import { Sparkles, X, Loader2, Search, FileText, Download, Tag } from 'lucide-react';
import { aiApi } from '../api/aiApi';
import { fileApi } from '../api/fileApi';
import { AiSearchResponse } from '../types/ai.types';

interface AiSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AiSearchModal: React.FC<AiSearchModalProps> = ({ isOpen, onClose }) => {
  const [prompt, setPrompt] = useState('');
  const [result, setResult] = useState<AiSearchResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  if (!isOpen) return null;

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!prompt.trim()) return;

    setIsLoading(true);
    try {
      const data = await aiApi.processAiSearch(prompt.trim());
      setResult(data);
    } catch (err) {
      console.error('AI search error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const samplePrompts = [
    'Find my PDF resumes uploaded recently',
    'Show ZIP archives larger than 1MB',
    'Find Word docs from last 30 days',
    'Images bigger than 500KB',
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/85 backdrop-blur-md">
      <div className="w-full max-w-xl glass-card rounded-2xl p-6 shadow-2xl relative border border-indigo-500/30 flex flex-col max-h-[85vh]">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-4 shrink-0">
          <div className="p-3 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 text-white shadow-lg shadow-indigo-500/25">
            <Sparkles className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              AI Natural Language Intelligence
              <span className="text-[10px] uppercase font-mono px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                Gemini Flash
              </span>
            </h3>
            <p className="text-xs text-slate-400">Ask VaultFlow anything in plain English</p>
          </div>
        </div>

        {/* Input Form */}
        <form onSubmit={handleSearch} className="space-y-3 mb-4 shrink-0">
          <div className="relative">
            <input
              type="text"
              autoFocus
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="e.g. Find PDF files larger than 2MB uploaded last week..."
              className="w-full pl-4 pr-12 py-3 rounded-xl glass-input text-sm focus:ring-2 focus:ring-purple-500 shadow-inner"
            />
            <button
              type="submit"
              disabled={isLoading || !prompt.trim()}
              className="absolute right-2 top-1/2 -translate-y-1/2 p-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white transition disabled:opacity-50"
            >
              {isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
            </button>
          </div>

          {/* Sample Chips */}
          <div className="flex flex-wrap gap-1.5 pt-1">
            {samplePrompts.map((chip) => (
              <button
                key={chip}
                type="button"
                onClick={() => setPrompt(chip)}
                className="px-2.5 py-1 rounded-lg bg-slate-900/60 hover:bg-slate-800 text-slate-400 hover:text-indigo-300 text-[11px] font-medium border border-slate-800 transition"
              >
                &ldquo;{chip}&rdquo;
              </button>
            ))}
          </div>
        </form>

        {/* Results Body */}
        {result && (
          <div className="flex-1 overflow-y-auto pr-1 space-y-4 pt-2 border-t border-slate-800">
            {/* AI Interpretation Banner */}
            <div className="p-3.5 rounded-xl bg-purple-950/30 border border-purple-500/30 text-xs space-y-1">
              <div className="flex items-center gap-1.5 text-purple-300 font-semibold uppercase tracking-wider text-[10px]">
                <Tag className="w-3 h-3" />
                AI Interpretation Summary
              </div>
              <p className="text-slate-200">{result.parsedFilter.interpretationSummary}</p>
            </div>

            {/* Results Grid */}
            <div className="space-y-2">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400">
                Matching Files ({result.matchingFiles.length})
              </span>

              {result.matchingFiles.length === 0 ? (
                <div className="py-8 text-center text-xs text-slate-500">
                  No files matched the parsed criteria.
                </div>
              ) : (
                result.matchingFiles.map((file) => (
                  <div
                    key={file.id}
                    className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 flex items-center justify-between text-xs"
                  >
                    <div className="flex items-center gap-3 truncate">
                      <FileText className="w-5 h-5 text-indigo-400 shrink-0" />
                      <div className="truncate">
                        <h4 className="font-bold text-slate-200 truncate">{file.originalName}</h4>
                        <span className="text-[10px] text-slate-400 font-mono">{file.formattedSize}</span>
                      </div>
                    </div>

                    <button
                      onClick={() => fileApi.downloadFile(file.id, file.originalName)}
                      className="p-2 rounded-lg text-slate-400 hover:text-emerald-400 hover:bg-slate-800 transition"
                    >
                      <Download className="w-4 h-4" />
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
