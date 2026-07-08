import { useDashboard } from "../hooks/useDashboard";
import StorageUsageCard from "../components/StorageUsageCard";
import StatCard from "../components/StatCard";
import RecentUploadsList from "../components/RecentUploadsList";
import FileTypeChart from "../components/FileTypeChart";

export default function DashboardPage() {
  const { data, isLoading } = useDashboard();

  if (isLoading || !data) {
    return <div className="p-6 text-slate-400 text-sm">Loading dashboard...</div>;
  }

  return (
    <div className="p-6 space-y-6">
      <h1 className="text-2xl font-semibold text-slate-800">Dashboard</h1>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StorageUsageCard data={data.storageUsage} />
        <StatCard label="Total Files" value={data.totalFiles} icon="📄" />
        <StatCard label="Total Folders" value={data.totalFolders} icon="📁" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div>
          <h2 className="text-sm font-medium text-slate-500 mb-3">Recent Uploads</h2>
          <RecentUploadsList items={data.recentUploads} />
        </div>
        <FileTypeChart data={data.fileTypeBreakdown} />
      </div>
    </div>
  );
}