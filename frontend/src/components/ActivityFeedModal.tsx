import React, { useState, useEffect, useCallback } from 'react';
import { Activity, X, Loader2, UploadCloud, Download, Trash2, Edit3, FolderInput, Share2, History, FolderPlus } from 'lucide-react';
import { activityApi } from '../api/activityApi';
import { ActivityLogItem } from '../types/activity.types';

interface ActivityFeedModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ActivityFeedModal: React.FC<ActivityFeedModalProps> = ({ isOpen, onClose }) => {
  const [activities, setActivities] = useState<ActivityLogItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const fetchActivities = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await activityApi.getActivityFeed(30);
      setActivities(data);
    } catch (err) {
      console.error('Failed to fetch activity feed:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      fetchActivities();
    }
  }, [isOpen, fetchActivities]);

  if (!isOpen) return null;

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'FILE_UPLOADED':
        return <UploadCloud className="w-4 h-4 text-indigo-400" />;
      case 'FILE_DOWNLOADED':
        return <Download className="w-4 h-4 text-emerald-400" />;
      case 'FILE_DELETED':
      case 'FOLDER_DELETED':
        return <Trash2 className="w-4 h-4 text-red-400" />;
      case 'FILE_RENAMED':
      case 'FOLDER_RENAMED':
        return <Edit3 className="w-4 h-4 text-amber-400" />;
      case 'FILE_MOVED':
      case 'FOLDER_MOVED':
        return <FolderInput className="w-4 h-4 text-blue-400" />;
      case 'FILE_SHARED':
        return <Share2 className="w-4 h-4 text-purple-400" />;
      case 'FILE_RESTORED':
        return <History className="w-4 h-4 text-cyan-400" />;
      case 'FOLDER_CREATED':
        return <FolderPlus className="w-4 h-4 text-amber-300" />;
      default:
        return <Activity className="w-4 h-4 text-slate-400" />;
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
      <div className="w-full max-w-md glass-card rounded-2xl p-6 shadow-2xl relative border border-slate-800 flex flex-col max-h-[85vh]">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-6 shrink-0">
          <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
            <Activity className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-white">Activity Log Feed</h3>
            <p className="text-xs text-slate-400">Recent workspace audit events</p>
          </div>
        </div>

        {/* Activity Feed List */}
        <div className="flex-1 overflow-y-auto pr-1 space-y-3">
          {isLoading ? (
            <div className="py-12 flex flex-col items-center justify-center text-slate-400 gap-2">
              <Loader2 className="w-6 h-6 animate-spin text-indigo-500" />
              <p className="text-xs">Loading audit feed...</p>
            </div>
          ) : activities.length === 0 ? (
            <div className="py-8 text-center text-xs text-slate-500">
              No recent audit activity records found.
            </div>
          ) : (
            activities.map((act) => (
              <div
                key={act.id}
                className="p-3.5 rounded-xl bg-slate-900/50 border border-slate-800 flex items-start gap-3 text-xs"
              >
                <div className="p-2 rounded-lg bg-slate-800 border border-slate-700 shrink-0 mt-0.5">
                  {getActivityIcon(act.activityType)}
                </div>

                <div className="flex-1 truncate">
                  <div className="flex items-center justify-between gap-2 mb-1">
                    <span className="font-bold text-slate-200 truncate">{act.entityName}</span>
                    <span className="text-[10px] text-slate-500 shrink-0">
                      {new Date(act.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <p className="text-slate-400 text-[11px] truncate">{act.details}</p>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
