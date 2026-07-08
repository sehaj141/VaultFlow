import { FileTypeBreakdown } from "../types/dashboard.types";

const COLORS = ["#6366f1", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#3b82f6"];

export default function FileTypeChart({ data }: { data: FileTypeBreakdown[] }) {
  const total = data.reduce((sum, d) => sum + d.count, 0);

  if (total === 0) {
    return <p className="text-sm text-slate-400">No files yet to break down by type.</p>;
  }

  return (
    <div className="bg-white rounded-xl p-6 shadow-sm border border-slate-100">
      <h3 className="text-sm font-medium text-slate-500 mb-4">File Types</h3>
      <div className="space-y-3">
        {data.map((item, idx) => {
          const pct = (item.count / total) * 100;
          return (
            <div key={item.extension}>
              <div className="flex justify-between text-xs text-slate-600 mb-1">
                <span className="uppercase font-medium">{item.extension}</span>
                <span>{item.count} files</span>
              </div>
              <div className="w-full bg-slate-100 rounded-full h-2">
                <div
                  className="h-2 rounded-full"
                  style={{ width: `${pct}%`, backgroundColor: COLORS[idx % COLORS.length] }}
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}