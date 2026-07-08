import { RecentItem } from "../types/dashboard.types";

const EXTENSION_ICONS: Record<string, string> = {
  pdf: "📄", docx: "📝", txt: "📃", zip: "🗜️", png: "🖼️", jpg: "🖼️",
};

function formatRelativeTime(dateStr: string): string {
  const diffMs = Date.now() - new Date(dateStr).getTime();
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 1) return "just now";
  if (diffMins < 60) return `${diffMins}m ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours}h ago`;
  return `${Math.floor(diffHours / 24)}d ago`;
}

export default function RecentUploadsList({ items }: { items: RecentItem[] }) {
  if (items.length === 0) {
    return <p className="text-sm text-slate-400">No uploads yet — drop a file to get started.</p>;
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-100 divide-y divide-slate-100">
      {items.map((item) => (
        <div key={item.id} className="flex items-center gap-3 px-4 py-3">
          <span className="text-xl">{EXTENSION_ICONS[item.extension ?? ""] ?? "📄"}</span>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-slate-800 truncate">{item.name}</p>
            <p className="text-xs text-slate-400">{formatRelativeTime(item.createdAt)}</p>
          </div>
        </div>
      ))}
    </div>
  );
}