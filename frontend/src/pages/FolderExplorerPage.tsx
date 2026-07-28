import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { folderApi } from '../api/folderApi';
import { fileApi } from '../api/fileApi';
import { Folder, Breadcrumb } from '../types/folder.types';
import { FileItem } from '../types/file.types';
import { CreateFolderModal } from '../components/CreateFolderModal';
import { RenameFolderModal } from '../components/RenameFolderModal';
import { MoveFolderModal } from '../components/MoveFolderModal';
import { UploadFileModal } from '../components/UploadFileModal';
import { RenameFileModal } from '../components/RenameFileModal';
import { MoveFileModal } from '../components/MoveFileModal';
import { ShareModal } from '../components/ShareModal';
import { VersionHistoryModal } from '../components/VersionHistoryModal';
import { ActivityFeedModal } from '../components/ActivityFeedModal';
import { TrashBinModal } from '../components/TrashBinModal';
import { AiSearchModal } from '../components/AiSearchModal';
import { FilePreviewModal } from '../components/FilePreviewModal';
import { DashboardAnalyticsView } from '../components/DashboardAnalyticsView';
import {
  Folder as FolderIcon,
  FolderPlus,
  UploadCloud,
  ChevronRight,
  Home,
  MoreVertical,
  Edit3,
  FolderInput,
  Trash2,
  Download,
  FileText,
  FileCode,
  FileArchive,
  Image as ImageIcon,
  LogOut,
  HardDrive,
  UserCheck,
  Loader2,
  FolderOpen,
  LayoutDashboard,
  FolderTree,
  Search,
  X,
  Filter,
  Share2,
  History,
  Activity,
  Sparkles,
  Eye,
} from 'lucide-react';

export const FolderExplorerPage: React.FC = () => {
  const { user, logout } = useAuth();

  // Tab State: 'dashboard' | 'explorer'
  const [activeTab, setActiveTab] = useState<'dashboard' | 'explorer'>('dashboard');

  const [currentFolderId, setCurrentFolderId] = useState<string | null>(null);
  const [currentFolder, setCurrentFolder] = useState<Folder | null>(null);
  const [subfolders, setSubfolders] = useState<Folder[]>([]);
  const [files, setFiles] = useState<FileItem[]>([]);
  const [breadcrumbs, setBreadcrumbs] = useState<Breadcrumb[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Search State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedExtension, setSelectedExtension] = useState<string>('');
  const [isSearching, setIsSearching] = useState(false);

  // Folder Modals
  const [isCreateFolderOpen, setIsCreateFolderOpen] = useState(false);
  const [renameFolderTarget, setRenameFolderTarget] = useState<Folder | null>(null);
  const [moveFolderTarget, setMoveFolderTarget] = useState<Folder | null>(null);

  // File Modals
  const [isUploadFileOpen, setIsUploadFileOpen] = useState(false);
  const [renameFileTarget, setRenameFileTarget] = useState<FileItem | null>(null);
  const [moveFileTarget, setMoveFileTarget] = useState<FileItem | null>(null);

  // Share, Version, Activity, Trash, AI & Preview Modals
  const [shareTarget, setShareTarget] = useState<{
    fileId?: string | null;
    folderId?: string | null;
    name: string;
  } | null>(null);
  const [versionTarget, setVersionTarget] = useState<FileItem | null>(null);
  const [previewTarget, setPreviewTarget] = useState<FileItem | null>(null);
  const [isActivityFeedOpen, setIsActivityFeedOpen] = useState(false);
  const [isTrashBinOpen, setIsTrashBinOpen] = useState(false);
  const [isAiSearchOpen, setIsAiSearchOpen] = useState(false);

  // Dropdown context menu state
  const [activeMenuId, setActiveMenuId] = useState<string | null>(null);

  const loadExplorerData = useCallback(async () => {
    if (activeTab !== 'explorer') return;
    setIsLoading(true);
    try {
      if (searchQuery.trim() || selectedExtension) {
        setIsSearching(true);
        const searchResults = await fileApi.searchFiles({
          query: searchQuery.trim(),
          extension: selectedExtension,
          folderId: currentFolderId,
        });
        setFiles(searchResults);
        setSubfolders([]);
      } else {
        setIsSearching(false);
        if (currentFolderId) {
          const folderData = await folderApi.getFolderById(currentFolderId);
          setCurrentFolder(folderData);
          setBreadcrumbs(folderData.breadcrumbs || []);
        } else {
          setCurrentFolder(null);
          setBreadcrumbs([]);
        }

        const [subfoldersData, filesData] = await Promise.all([
          folderApi.getSubfolders(currentFolderId),
          fileApi.getFiles(currentFolderId),
        ]);

        setSubfolders(subfoldersData);
        setFiles(filesData);
      }
    } catch (error) {
      console.error('Failed to fetch explorer items:', error);
    } finally {
      setIsLoading(false);
    }
  }, [currentFolderId, activeTab, searchQuery, selectedExtension]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadExplorerData();
    }, 300);
    return () => clearTimeout(timer);
  }, [loadExplorerData]);

  // Folder Actions
  const handleCreateFolder = async (name: string) => {
    await folderApi.createFolder({ name, parentId: currentFolderId });
    await loadExplorerData();
  };

  const handleRenameFolder = async (id: string, newName: string) => {
    await folderApi.renameFolder(id, { newName });
    await loadExplorerData();
  };

  const handleMoveFolder = async (id: string, targetParentId: string | null) => {
    await folderApi.moveFolder(id, { targetParentId });
    await loadExplorerData();
  };

  const handleDeleteFolder = async (id: string) => {
    if (window.confirm('Move folder to trash? Subfolders will also be trashed.')) {
      await folderApi.deleteFolder(id);
      await loadExplorerData();
    }
  };

  // File Actions
  const handleUploadFile = async (file: File) => {
    await fileApi.uploadFile(file, currentFolderId);
    await loadExplorerData();
  };

  const handleDownloadFile = async (file: FileItem) => {
    await fileApi.downloadFile(file.id, file.originalName);
  };

  const handleRenameFile = async (id: string, newName: string) => {
    await fileApi.renameFile(id, { newName });
    await loadExplorerData();
  };

  const handleMoveFile = async (id: string, targetFolderId: string | null) => {
    await fileApi.moveFile(id, { targetFolderId });
    await loadExplorerData();
  };

  const handleDeleteFile = async (id: string) => {
    if (window.confirm('Move this file to trash?')) {
      await fileApi.deleteFile(id);
      await loadExplorerData();
    }
  };

  const getFileIcon = (ext: string) => {
    const cleanExt = ext.toLowerCase();
    if (['png', 'jpg', 'jpeg', 'svg'].includes(cleanExt)) {
      return <ImageIcon className="w-6 h-6 text-purple-400" />;
    }
    if (cleanExt === 'pdf') {
      return <FileText className="w-6 h-6 text-red-400" />;
    }
    if (cleanExt === 'docx' || cleanExt === 'txt') {
      return <FileCode className="w-6 h-6 text-blue-400" />;
    }
    if (cleanExt === 'zip') {
      return <FileArchive className="w-6 h-6 text-amber-400" />;
    }
    return <FileText className="w-6 h-6 text-slate-400" />;
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Navbar */}
      <header className="glass-card px-6 py-4 border-b border-slate-800 flex items-center justify-between sticky top-0 z-20">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
            <HardDrive className="w-6 h-6" />
          </div>
          <div>
            <h1 className="font-bold text-lg text-white leading-tight">VaultFlow</h1>
            <p className="text-xs text-slate-400">Intelligent Cloud Storage Platform</p>
          </div>
        </div>

        {/* Center Tab View Selector */}
        <div className="hidden sm:flex items-center gap-1 bg-slate-900/80 p-1.5 rounded-xl border border-slate-800">
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition ${
              activeTab === 'dashboard'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <LayoutDashboard className="w-4 h-4" />
            Dashboard
          </button>
          <button
            onClick={() => setActiveTab('explorer')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition ${
              activeTab === 'explorer'
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <FolderTree className="w-4 h-4" />
            File Explorer
          </button>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setIsAiSearchOpen(true)}
            className="px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white text-xs font-semibold shadow-lg shadow-purple-600/20 flex items-center gap-2 transition"
          >
            <Sparkles className="w-4 h-4" />
            <span>AI Search</span>
          </button>

          <button
            onClick={() => setIsActivityFeedOpen(true)}
            className="p-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 flex items-center gap-2 text-xs font-semibold transition"
            title="View Activity Feed"
          >
            <Activity className="w-4 h-4 text-indigo-400" />
            <span className="hidden lg:inline">Activity Log</span>
          </button>

          <button
            onClick={() => setIsTrashBinOpen(true)}
            className="p-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-red-400 border border-slate-800 flex items-center gap-2 text-xs font-semibold transition"
            title="Open Trash Bin"
          >
            <Trash2 className="w-4 h-4" />
            <span className="hidden lg:inline">Trash Bin</span>
          </button>

          <div className="flex items-center gap-2 bg-slate-900/60 px-3 py-1.5 rounded-lg border border-slate-800">
            <UserCheck className="w-4 h-4 text-emerald-400" />
            <span className="text-sm font-medium">{user?.fullName}</span>
          </div>

          <button
            onClick={logout}
            className="flex items-center gap-2 px-3.5 py-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 text-sm font-medium transition border border-red-500/20"
          >
            <LogOut className="w-4 h-4" />
            Sign Out
          </button>
        </div>
      </header>

      {/* Main Workspace Body */}
      <div className="p-6 max-w-7xl mx-auto w-full flex-1 flex flex-col gap-6">
        {activeTab === 'dashboard' ? (
          <DashboardAnalyticsView onNavigateToExplorer={() => setActiveTab('explorer')} />
        ) : (
          <>
            {/* Search Bar & Filter Controls */}
            <div className="glass-card p-4 rounded-2xl space-y-4">
              <div className="flex flex-col md:flex-row items-center justify-between gap-4">
                <div className="relative w-full md:w-96">
                  <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search files by name..."
                    className="w-full pl-10 pr-9 py-2.5 rounded-xl glass-input text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                  {searchQuery && (
                    <button
                      onClick={() => setSearchQuery('')}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  )}
                </div>

                <div className="flex items-center gap-2 overflow-x-auto w-full md:w-auto py-1">
                  <Filter className="w-4 h-4 text-slate-500 shrink-0" />
                  <span className="text-xs text-slate-400 shrink-0 font-medium">Type:</span>

                  {[
                    { label: 'All', value: '' },
                    { label: 'PDF', value: 'pdf' },
                    { label: 'PNG/JPEG', value: 'png' },
                    { label: 'DOCX', value: 'docx' },
                    { label: 'ZIP', value: 'zip' },
                  ].map((filter) => (
                    <button
                      key={filter.label}
                      onClick={() => setSelectedExtension(filter.value)}
                      className={`px-3 py-1 rounded-lg text-xs font-semibold transition shrink-0 ${
                        selectedExtension === filter.value
                          ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                          : 'bg-slate-900/60 text-slate-400 hover:text-slate-200 border border-slate-800'
                      }`}
                    >
                      {filter.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Breadcrumb Navigation & Action Buttons */}
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pt-3 border-t border-slate-800/80">
                <nav className="flex items-center gap-1.5 text-sm overflow-x-auto max-w-full py-1">
                  <button
                    onClick={() => {
                      setCurrentFolderId(null);
                      setSearchQuery('');
                    }}
                    className={`flex items-center gap-1.5 px-2.5 py-1 rounded-lg transition ${
                      currentFolderId === null
                        ? 'text-indigo-400 font-bold bg-indigo-500/10'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                    }`}
                  >
                    <Home className="w-4 h-4" />
                    <span>Root</span>
                  </button>

                  {breadcrumbs.map((crumb) => (
                    <React.Fragment key={crumb.id}>
                      <ChevronRight className="w-4 h-4 text-slate-600 shrink-0" />
                      <button
                        onClick={() => {
                          setCurrentFolderId(crumb.id);
                          setSearchQuery('');
                        }}
                        className={`px-2.5 py-1 rounded-lg transition truncate max-w-[150px] ${
                          crumb.id === currentFolderId
                            ? 'text-indigo-400 font-bold bg-indigo-500/10'
                            : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                        }`}
                      >
                        {crumb.name}
                      </button>
                    </React.Fragment>
                  ))}
                </nav>

                <div className="flex items-center gap-3 shrink-0">
                  <button
                    onClick={() => setIsCreateFolderOpen(true)}
                    className="px-3.5 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 text-sm font-semibold border border-slate-700 flex items-center gap-2 transition"
                  >
                    <FolderPlus className="w-4 h-4 text-amber-400" />
                    New Folder
                  </button>

                  <button
                    onClick={() => setIsUploadFileOpen(true)}
                    className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-semibold shadow-lg shadow-indigo-600/25 flex items-center gap-2 transition"
                  >
                    <UploadCloud className="w-4 h-4" />
                    Upload File
                  </button>
                </div>
              </div>
            </div>

            {/* Explorer Content */}
            <div className="flex-1 space-y-8">
              {isLoading ? (
                <div className="py-20 flex flex-col items-center justify-center text-slate-400 gap-3">
                  <Loader2 className="w-8 h-8 animate-spin text-indigo-500" />
                  <p className="text-sm font-medium font-mono">Executing query...</p>
                </div>
              ) : subfolders.length === 0 && files.length === 0 ? (
                <div className="glass-card rounded-2xl py-16 px-4 text-center flex flex-col items-center justify-center">
                  <div className="w-16 h-16 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-center text-slate-500 mb-4">
                    {isSearching ? <Search className="w-8 h-8 text-indigo-400" /> : <FolderOpen className="w-8 h-8" />}
                  </div>
                  <h3 className="text-lg font-bold text-slate-200 mb-1">
                    {isSearching ? 'No matching files found' : 'This directory is empty'}
                  </h3>
                  <p className="text-sm text-slate-400 max-w-md mb-6">
                    {isSearching
                      ? 'Try adjusting your search query or filter tags.'
                      : 'Upload PDFs, Word docs, images, ZIPs, or create folders to organize your workspace.'}
                  </p>
                  {!isSearching && (
                    <div className="flex items-center gap-3">
                      <button
                        onClick={() => setIsCreateFolderOpen(true)}
                        className="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm font-semibold transition"
                      >
                        Create Folder
                      </button>
                      <button
                        onClick={() => setIsUploadFileOpen(true)}
                        className="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-semibold transition flex items-center gap-2"
                      >
                        <UploadCloud className="w-4 h-4" />
                        Upload File
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <>
                  {/* Folders Section */}
                  {subfolders.length > 0 && !isSearching && (
                    <div>
                      <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 px-1">
                        Folders ({subfolders.length})
                      </h3>
                      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {subfolders.map((folder) => (
                          <div
                            key={folder.id}
                            className="glass-card rounded-2xl p-4 group hover:border-amber-500/40 transition relative flex flex-col justify-between cursor-pointer"
                            onClick={() => setCurrentFolderId(folder.id)}
                          >
                            <div className="flex items-start justify-between gap-3 mb-3">
                              <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20 group-hover:scale-105 transition">
                                <FolderIcon className="w-6 h-6 fill-amber-500/20" />
                              </div>

                              <div className="relative" onClick={(e) => e.stopPropagation()}>
                                <button
                                  onClick={() => setActiveMenuId(activeMenuId === folder.id ? null : folder.id)}
                                  className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
                                >
                                  <MoreVertical className="w-4 h-4" />
                                </button>

                                {activeMenuId === folder.id && (
                                  <div className="absolute right-0 top-8 w-44 glass-card rounded-xl shadow-xl py-1 z-30 border border-slate-800 text-xs">
                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setShareTarget({ folderId: folder.id, name: folder.name });
                                      }}
                                      className="w-full px-3 py-2 text-left text-indigo-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <Share2 className="w-3.5 h-3.5 text-indigo-400" />
                                      Share Link
                                    </button>

                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setRenameFolderTarget(folder);
                                      }}
                                      className="w-full px-3 py-2 text-left text-slate-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <Edit3 className="w-3.5 h-3.5 text-amber-400" />
                                      Rename
                                    </button>

                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setMoveFolderTarget(folder);
                                      }}
                                      className="w-full px-3 py-2 text-left text-slate-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <FolderInput className="w-3.5 h-3.5 text-blue-400" />
                                      Move
                                    </button>
                                    <div className="my-1 border-t border-slate-800" />
                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        handleDeleteFolder(folder.id);
                                      }}
                                      className="w-full px-3 py-2 text-left text-red-400 hover:bg-red-500/10 flex items-center gap-2"
                                    >
                                      <Trash2 className="w-3.5 h-3.5" />
                                      Trash
                                    </button>
                                  </div>
                                )}
                              </div>
                            </div>

                            <div>
                              <h4 className="font-bold text-white text-sm truncate group-hover:text-amber-300 transition">
                                {folder.name}
                              </h4>
                              <p className="text-xs text-slate-400 mt-1">
                                {folder.subfolderCount} subfolder{folder.subfolderCount === 1 ? '' : 's'}
                              </p>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Files Section */}
                  {files.length > 0 && (
                    <div>
                      <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3 px-1">
                        {isSearching ? `Search Matches (${files.length})` : `Files (${files.length})`}
                      </h3>
                      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {files.map((file) => (
                          <div
                            key={file.id}
                            onClick={() => setPreviewTarget(file)}
                            className="glass-card rounded-2xl p-4 group hover:border-indigo-500/40 transition relative flex flex-col justify-between cursor-pointer"
                          >
                            <div className="flex items-start justify-between gap-3 mb-3">
                              <div className="p-3 rounded-xl bg-slate-900 border border-slate-800 group-hover:scale-105 transition">
                                {getFileIcon(file.extension)}
                              </div>

                              <div className="relative" onClick={(e) => e.stopPropagation()}>
                                <button
                                  onClick={() => setActiveMenuId(activeMenuId === file.id ? null : file.id)}
                                  className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
                                >
                                  <MoreVertical className="w-4 h-4" />
                                </button>

                                {activeMenuId === file.id && (
                                  <div className="absolute right-0 top-8 w-44 glass-card rounded-xl shadow-xl py-1 z-30 border border-slate-800 text-xs">
                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setPreviewTarget(file);
                                      }}
                                      className="w-full px-3 py-2 text-left text-emerald-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <Eye className="w-3.5 h-3.5 text-emerald-400" />
                                      Preview PDF/Image
                                    </button>

                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setShareTarget({ fileId: file.id, name: file.originalName });
                                      }}
                                      className="w-full px-3 py-2 text-left text-indigo-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <Share2 className="w-3.5 h-3.5 text-indigo-400" />
                                      Share Link
                                    </button>

                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setVersionTarget(file);
                                      }}
                                      className="w-full px-3 py-2 text-left text-cyan-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <History className="w-3.5 h-3.5 text-cyan-400" />
                                      Version History
                                    </button>

                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        handleDownloadFile(file);
                                      }}
                                      className="w-full px-3 py-2 text-left text-slate-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <Download className="w-3.5 h-3.5 text-emerald-400" />
                                      Download
                                    </button>
                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setRenameFileTarget(file);
                                      }}
                                      className="w-full px-3 py-2 text-left text-slate-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <Edit3 className="w-3.5 h-3.5 text-amber-400" />
                                      Rename
                                    </button>
                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        setMoveFileTarget(file);
                                      }}
                                      className="w-full px-3 py-2 text-left text-slate-300 hover:bg-slate-800 hover:text-white flex items-center gap-2"
                                    >
                                      <FolderInput className="w-3.5 h-3.5 text-cyan-400" />
                                      Move
                                    </button>
                                    <div className="my-1 border-t border-slate-800" />
                                    <button
                                      onClick={() => {
                                        setActiveMenuId(null);
                                        handleDeleteFile(file.id);
                                      }}
                                      className="w-full px-3 py-2 text-left text-red-400 hover:bg-red-500/10 flex items-center gap-2"
                                    >
                                      <Trash2 className="w-3.5 h-3.5" />
                                      Trash
                                    </button>
                                  </div>
                                )}
                              </div>
                            </div>

                            <div>
                              <h4 className="font-bold text-white text-sm truncate group-hover:text-indigo-300 transition">
                                {file.originalName}
                              </h4>
                              <div className="flex items-center justify-between text-xs text-slate-400 mt-2">
                                <span>{file.formattedSize}</span>
                                <span className="uppercase text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono">
                                  {file.extension}
                                </span>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          </>
        )}
      </div>

      {/* Modals */}
      <CreateFolderModal
        isOpen={isCreateFolderOpen}
        onClose={() => setIsCreateFolderOpen(false)}
        onSubmit={handleCreateFolder}
        parentFolderName={currentFolder ? currentFolder.name : 'Root Space'}
      />

      <RenameFolderModal
        folder={renameFolderTarget}
        isOpen={!!renameFolderTarget}
        onClose={() => setRenameFolderTarget(null)}
        onSubmit={handleRenameFolder}
      />

      <MoveFolderModal
        folder={moveFolderTarget}
        isOpen={!!moveFolderTarget}
        onClose={() => setMoveFolderTarget(null)}
        onSubmit={handleMoveFolder}
      />

      <UploadFileModal
        isOpen={isUploadFileOpen}
        onClose={() => setIsUploadFileOpen(false)}
        onSubmit={handleUploadFile}
        targetFolderName={currentFolder ? currentFolder.name : 'Root Space'}
      />

      <RenameFileModal
        file={renameFileTarget}
        isOpen={!!renameFileTarget}
        onClose={() => setRenameFileTarget(null)}
        onSubmit={handleRenameFile}
      />

      <MoveFileModal
        file={moveFileTarget}
        isOpen={!!moveFileTarget}
        onClose={() => setMoveFileTarget(null)}
        onSubmit={handleMoveFile}
      />

      <ShareModal
        isOpen={!!shareTarget}
        onClose={() => setShareTarget(null)}
        fileId={shareTarget?.fileId}
        folderId={shareTarget?.folderId}
        resourceName={shareTarget?.name || ''}
      />

      <VersionHistoryModal
        file={versionTarget}
        isOpen={!!versionTarget}
        onClose={() => setVersionTarget(null)}
        onVersionRestored={loadExplorerData}
      />

      <ActivityFeedModal
        isOpen={isActivityFeedOpen}
        onClose={() => setIsActivityFeedOpen(false)}
      />

      <TrashBinModal
        isOpen={isTrashBinOpen}
        onClose={() => setIsTrashBinOpen(false)}
        onItemRestored={loadExplorerData}
      />

      <AiSearchModal
        isOpen={isAiSearchOpen}
        onClose={() => setIsAiSearchOpen(false)}
      />

      <FilePreviewModal
        file={previewTarget}
        isOpen={!!previewTarget}
        onClose={() => setPreviewTarget(null)}
      />
    </div>
  );
};
