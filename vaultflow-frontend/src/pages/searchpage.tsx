import { useState, useCallback } from "react";
import { useSearch } from "../hooks/useSearch";
import { SearchParams } from "../types/search.types";
import { FileItem } from "../types/file.types";

const EXTENSIONS = ["pdf", "docx", "txt", "zip", "png", "jpg"];
const EXTENSION_ICONS: Record<string, string> = {
  pdf: "📄", docx: "📝", txt: "📃", zip: "🗜️", png: "🖼️", jpg: "🖼️",
};

function formatBytes(bytes: number): string {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function highlight(text: string, query: string | null): JSX.Element {
  if (!query) return <>{text}</>;
  const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`, "gi");
  const parts = text.split(regex);
  return (
    <>
      {parts.map((part, i) =>
        regex.test(part) ? (
          <mark key={i} className="bg-yellow-100 text-yellow-900 rounded px-0.5">
            {part}
          </mark>
        ) : (
          <span key={i}>{part}</span>
        )
      )}
    </>
  );
}

export default function SearchPage() {
  const [rawQuery, setRawQuery] = useState("");
  const [committedQuery, setCommittedQuery] = useState("");
  const [extension, setExtension] = useState("");
  const [uploadedAfter, setUploadedAfter] = useState("");
  const [uploadedBefore, setUploadedBefore] = useState("");
  const [page, setPage] = useState(0);

  const params: SearchParams = {
    query: committedQuery || undefined,
    extension: extension || undefined,
    uploadedAfter: uploadedAfter || undefined,
    uploadedBefore: uploadedBefore || undefined,
    page,
    size: 20,
  };

  const hasAnyFilter = !!(committedQuery || extension || uploadedAfter || uploadedBefore);
  const { data, isFetching } = useSearch(params, hasAnyFilter);

  const handleSearch = useCallback(() => {
    setPage(0);
    setCommittedQuery(rawQuery);
  }, [rawQuery]);

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-semibold text-slate-800">Search</h1>

      {/* Search bar */}
      <div className="flex gap-2">
        <input
          type="text"
          placeholder='Try "machine learning" or "resume"'
          value={rawQuery}
          onChange={(e) => setRawQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
          className="flex-1 border border-slate-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
        />
        <button
          onClick={handleSearch}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700"
        >
          Search
        </button>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-3">
        <select
          value={extension}
          onChange={(e) => { setExtension(e.target.value); setPage(0); }}
          className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
        >
          <option value="">All Types</option>
          {EXTENSIONS.map((ext) => (
            <option key={ext} value={ext}>{ext.toUpperCase()}</option>
          ))}
        </select>

        <div className="flex items-center gap-2 text-sm text-slate-500">
          <label>From</label>
          <input
            type="date"
            value={uploadedAfter}
            onChange={(e) => { setUploadedAfter(e.target.value); setPage(0); }}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-2 text-sm text-slate-500">
          <label>To</label>
          <input
            type="date"
            value={uploadedBefore}
            onChange={(e) => { setUploadedBefore(e.target.value); setPage(0); }}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
          />
        </div>

        {(extension || uploadedAfter || uploadedBefore) && (
          <button
            onClick={() => {
              setExtension("");
              setUploadedAfter("");
              setUploadedBefore("");
              setPage(0);
            }}
            className="text-xs text-slate-400 hover:text-slate-600 underline"
          >
            Clear filters
          </button>
        )}
      </div>

      {/* Results */}
      {!hasAnyFilter ? (
        <p className="text-slate-400 text-sm">Enter a search term or select a filter to get started.</p>
      ) : isFetching ? (
        <p className="text-slate-400 text-sm">Searching...</p>
      ) : !data || data.results.length === 0 ? (
        <p className="text-slate-500 text-sm">No results found. Try a different term or filter.</p>
      ) : (
        <>
          <p className="text-xs text-slate-400">
            {data.totalResults} result{data.totalResults !== 1 ? "s" : ""}
            {committedQuery && ` for "${committedQuery}"`}
          </p>

          <div className="divide-y divide-slate-100 border border-slate-100 rounded-xl overflow-hidden">
            {data.results.map((file: FileItem) => (
              <div key={file.id} className="flex items-center gap-3 px-4 py-3 bg-white hover:bg-slate-50 transition">
                <span className="text-2xl">{EXTENSION_ICONS[file.extension] ?? "📄"}</span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-slate-800 truncate">
                    {highlight(file.name, data.queryEchoed)}
                  </p>
                  <p className="text-xs text-slate-400">
                    {file.extension.toUpperCase()} · {formatBytes(file.sizeBytes)} ·{" "}
                    {new Date(file.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {data.totalPages > 1 && (
            <div className="flex justify-between items-center text-sm text-slate-600">
              <button
                onClick={() => setPage((p) => Math.max(p - 1, 0))}
                disabled={page === 0}
                className="px-3 py-1.5 border border-slate-300 rounded-lg disabled:opacity-40 hover:bg-slate-50"
              >
                ← Previous
              </button>
              <span>
                Page {page + 1} of {data.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(p + 1, data.totalPages - 1))}
                disabled={page >= data.totalPages - 1}
                className="px-3 py-1.5 border border-slate-300 rounded-lg disabled:opacity-40 hover:bg-slate-50"
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}