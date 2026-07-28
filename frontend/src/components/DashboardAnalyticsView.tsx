import React, { useEffect, useState } from 'react';
import { dashboardApi } from '../api/dashboardApi';
import { DashboardAnalytics } from '../types/dashboard.types';
import {
  HardDrive,
  FileText,
  Folder as FolderIcon,
  PieChart,
  Clock,
  ArrowUpRight,
  Loader2,
  Image as ImageIcon,
  FileCode,
  FileArchive,
  ShieldCheck,
  Zap,
  Activity,
  Cpu,
  Database,
  Sparkles,
  UploadCloud,
  FolderPlus,
} from 'lucide-react';

interface DashboardAnalyticsViewProps {
  onNavigateToExplorer: () => void;
}

export const DashboardAnalyticsView: React.FC<DashboardAnalyticsViewProps> = ({ onNavigateToExplorer }) => {
  const [data, setData] = useState<DashboardAnalytics | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const analytics = await dashboardApi.getAnalytics();
        setData(analytics);
      } catch (error) {
        console.error('Failed to load dashboard analytics:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchAnalytics();
  }, []);

  if (isLoading) {
    return (
      <div className="py-28 flex flex-col items-center justify-center text-slate-400 gap-3">
        <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
        <p className="text-sm font-medium font-mono">Aggregating Cloud Storage Metrics...</p>
      </div>
    );
  }

  if (!data) return null;

  const getCategoryColor = (name: string) => {
    switch (name) {
      case 'PDF Documents':
        return 'from-rose-500 to-red-600';
      case 'Images':
        return 'from-purple-500 to-indigo-600';
      case 'Text & Docs':
        return 'from-cyan-500 to-blue-600';
      case 'Archives':
        return 'from-amber-400 to-orange-500';
      default:
        return 'from-slate-500 to-slate-600';
    }
  };

  const getCategoryIcon = (name: string) => {
    switch (name) {
      case 'PDF Documents':
        return <FileText className="w-4 h-4 text-rose-400" />;
      case 'Images':
        return <ImageIcon className="w-4 h-4 text-purple-400" />;
      case 'Text & Docs':
        return <FileCode className="w-4 h-4 text-cyan-400" />;
      case 'Archives':
        return <FileArchive className="w-4 h-4 text-amber-400" />;
      default:
        return <FileText className="w-4 h-4 text-slate-400" />;
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Storage Capacity Banner Card */}
      <div className="glass-card rounded-3xl p-7 relative overflow-hidden border border-indigo-500/20 shadow-2xl">
        {/* Glow Effects */}
        <div className="absolute -top-20 -right-20 w-96 h-96 bg-indigo-600/15 rounded-full blur-3xl pointer-events-none animate-pulse" />
        <div className="absolute -bottom-20 -left-20 w-80 h-80 bg-purple-600/15 rounded-full blur-3xl pointer-events-none" />

        <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-6 relative z-10">
          <div className="flex items-center gap-5">
            <div className="p-4.5 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-700 text-white shadow-xl shadow-indigo-600/30 border border-indigo-400/30">
              <HardDrive className="w-9 h-9" />
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h2 className="text-2xl font-extrabold text-white tracking-tight">Cloud Storage Capacity</h2>
                <span className="inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full text-[11px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                  Operational
                </span>
              </div>
              <p className="text-sm text-slate-400 mt-1">
                {data.formattedUsedStorage} used of{' '}
                <span className="text-indigo-300 font-bold">{data.formattedMaxStorage}</span> Enterprise Quota Limit
              </p>
            </div>
          </div>

          <div className="flex items-center gap-6">
            <div className="text-right">
              <div className="text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-indigo-300 via-purple-300 to-cyan-300 font-mono">
                {data.usagePercentage}%
              </div>
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Total Quota Allocated</span>
            </div>
          </div>
        </div>

        {/* Dynamic Progress Bar */}
        <div className="w-full bg-slate-900/90 h-3.5 rounded-full overflow-hidden mt-6 border border-slate-800 p-0.5 shadow-inner">
          <div
            className="bg-gradient-to-r from-indigo-500 via-purple-500 to-cyan-400 h-full rounded-full transition-all duration-700 shadow-lg shadow-indigo-500/40"
            style={{ width: `${Math.max(data.usagePercentage, 2)}%` }}
          />
        </div>
      </div>

      {/* Analytics Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Metric 1: Total Files */}
        <div className="glass-card rounded-2xl p-5 border border-slate-800/80 hover:border-indigo-500/30 transition shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Total Files</span>
            <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <FileText className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <h3 className="text-2xl font-black text-white">{data.totalFilesCount}</h3>
            <p className="text-xs text-emerald-400 flex items-center gap-1 mt-1 font-medium">
              <Zap className="w-3 h-3" /> Indexed in PostgreSQL
            </p>
          </div>
        </div>

        {/* Metric 2: Total Folders */}
        <div className="glass-card rounded-2xl p-5 border border-slate-800/80 hover:border-amber-500/30 transition shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Workspace Folders</span>
            <div className="p-2.5 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <FolderIcon className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <h3 className="text-2xl font-black text-white">{data.totalFoldersCount}</h3>
            <p className="text-xs text-amber-400 flex items-center gap-1 mt-1 font-medium">
              <ShieldCheck className="w-3 h-3" /> Materialized Path Hierarchy
            </p>
          </div>
        </div>

        {/* Metric 3: Active Quota Usage */}
        <div className="glass-card rounded-2xl p-5 border border-slate-800/80 hover:border-purple-500/30 transition shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Used Storage</span>
            <div className="p-2.5 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
              <HardDrive className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <h3 className="text-2xl font-black text-white">{data.formattedUsedStorage}</h3>
            <p className="text-xs text-indigo-400 flex items-center gap-1 mt-1 font-medium">
              <PieChart className="w-3 h-3" /> {data.usagePercentage}% Of Max Quota
            </p>
          </div>
        </div>

        {/* Metric 4: System Architecture */}
        <div className="glass-card rounded-2xl p-5 border border-slate-800/80 hover:border-emerald-500/30 transition shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Architecture</span>
            <div className="p-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Cpu className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-3">
            <h3 className="text-lg font-bold text-white">Spring Boot 3 + React</h3>
            <p className="text-xs text-emerald-400 flex items-center gap-1 mt-1 font-medium">
              <Database className="w-3 h-3" /> Async Audit Logging Engine
            </p>
          </div>
        </div>
      </div>

      {/* Main Grid: Category Breakdown & Recent Uploads */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Breakdown Card */}
        <div className="glass-card rounded-3xl p-6 border border-slate-800 flex flex-col justify-between shadow-xl">
          <div>
            <div className="flex items-center justify-between mb-5">
              <h3 className="font-bold text-lg text-white flex items-center gap-2.5">
                <PieChart className="w-5 h-5 text-indigo-400" />
                Category Distribution
              </h3>
              <span className="text-xs text-slate-400 font-mono">B-Tree Indexed</span>
            </div>

            {data.categoryBreakdown.length === 0 ? (
              <div className="py-12 text-center text-xs text-slate-500">No files uploaded yet.</div>
            ) : (
              <div className="space-y-4">
                {data.categoryBreakdown.map((cat) => (
                  <div key={cat.categoryName} className="space-y-2">
                    <div className="flex items-center justify-between text-xs">
                      <div className="flex items-center gap-2">
                        {getCategoryIcon(cat.categoryName)}
                        <span className="font-bold text-slate-200">{cat.categoryName}</span>
                        <span className="text-[11px] text-slate-400">({cat.fileCount} files)</span>
                      </div>
                      <span className="font-mono text-xs text-indigo-300 font-bold">{cat.formattedSize}</span>
                    </div>

                    <div className="w-full bg-slate-900 h-2.5 rounded-full overflow-hidden border border-slate-800 p-0.5">
                      <div
                        className={`h-full rounded-full bg-gradient-to-r ${getCategoryColor(
                          cat.categoryName
                        )} transition-all duration-500 shadow-md`}
                        style={{ width: `${Math.max(cat.percentageOfTotalStorage, 4)}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Recent Upload Activity Card */}
        <div className="glass-card rounded-3xl p-6 border border-slate-800 flex flex-col justify-between shadow-xl">
          <div>
            <div className="flex items-center justify-between mb-5">
              <h3 className="font-bold text-lg text-white flex items-center gap-2.5">
                <Clock className="w-5 h-5 text-cyan-400" />
                Recent Upload Stream
              </h3>
              <button
                onClick={onNavigateToExplorer}
                className="text-xs text-indigo-400 hover:text-indigo-300 font-bold flex items-center gap-1 transition"
              >
                File Explorer
                <ArrowUpRight className="w-3.5 h-3.5" />
              </button>
            </div>

            {data.recentUploads.length === 0 ? (
              <div className="py-12 text-center text-xs text-slate-500">No recent uploads in stream.</div>
            ) : (
              <div className="space-y-3">
                {data.recentUploads.map((file) => (
                  <div
                    key={file.id}
                    onClick={onNavigateToExplorer}
                    className="p-3.5 rounded-2xl bg-slate-900/60 border border-slate-800 hover:border-indigo-500/40 flex items-center justify-between transition cursor-pointer group"
                  >
                    <div className="flex items-center gap-3.5 truncate">
                      <div className="p-2.5 rounded-xl bg-slate-800 border border-slate-700/60 group-hover:scale-105 transition shrink-0">
                        <FileText className="w-4 h-4 text-indigo-400" />
                      </div>
                      <div className="truncate">
                        <h4 className="text-sm font-bold text-slate-200 group-hover:text-indigo-300 transition truncate">
                          {file.originalName}
                        </h4>
                        <p className="text-xs text-slate-400 font-mono mt-0.5">{file.formattedSize}</p>
                      </div>
                    </div>

                    <span className="text-[10px] uppercase font-mono px-2 py-0.5 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 shrink-0 font-bold">
                      {file.extension}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Quick Operations Control Bar */}
          <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium">Quick Operations</span>
            <div className="flex items-center gap-2">
              <button
                onClick={onNavigateToExplorer}
                className="px-3 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold flex items-center gap-1.5 transition shadow-lg shadow-indigo-600/20"
              >
                <UploadCloud className="w-3.5 h-3.5" />
                Upload
              </button>
              <button
                onClick={onNavigateToExplorer}
                className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold flex items-center gap-1.5 transition border border-slate-700"
              >
                <FolderPlus className="w-3.5 h-3.5 text-amber-400" />
                New Folder
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
