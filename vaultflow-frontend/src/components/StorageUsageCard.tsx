import { StorageUsage } from "../types/dashboard.types";

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
}

export default function StorageUsageCard({ data }: { data: StorageUsage }) {
  const isNearLimit = data.percentageUsed > 85;

  return (
    <div className="bg-white rounded-xl p-6 shadow-sm border border-slate-100">
      <h3 className="text-sm font-medium text-slate-500 mb-3">Storage Usage</h3>

      <div className="w-full bg-slate-100 rounded-full h-3 mb-2">
        <div
          className={`h-3 rounded-full transition-all ${isNearLimit ? "bg-red-500" : "bg-indigo-600"}`}
          style={{ width: `${Math.min(data.percentageUsed, 100)}%` }}
        />
      </div>

      <p className="text-sm text-slate-600">
        {formatBytes(data.usedBytes)} of {formatBytes(data.limitBytes)} used
      </p>
      <p className={`text-xs mt-1 ${isNearLimit ? "text-red-500" : "text-slate-400"}`}>
        {data.percentageUsed}% used
      </p>
    </div>
  );
}