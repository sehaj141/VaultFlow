import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, Folder, HardDrive, ShieldCheck, UserCheck } from 'lucide-react';

export const DashboardPlaceholder: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 flex flex-col">
      {/* Top Navbar */}
      <header className="flex items-center justify-between glass-card p-4 rounded-2xl mb-8">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
            <HardDrive className="w-6 h-6" />
          </div>
          <div>
            <h1 className="font-bold text-lg text-white">VaultFlow</h1>
            <p className="text-xs text-slate-400">Phase 1 - Protected Workspace</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 bg-slate-900/60 px-3 py-1.5 rounded-lg border border-slate-800">
            <UserCheck className="w-4 h-4 text-emerald-400" />
            <span className="text-sm font-medium">{user?.fullName}</span>
            <span className="text-xs px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
              {user?.role}
            </span>
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

      {/* Main Container */}
      <main className="flex-1 flex flex-col items-center justify-center text-center p-8 glass-card rounded-2xl max-w-4xl mx-auto w-full">
        <div className="w-16 h-16 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-6">
          <ShieldCheck className="w-10 h-10" />
        </div>

        <h2 className="text-3xl font-extrabold text-white tracking-tight mb-3">
          Authentication Complete!
        </h2>
        <p className="text-slate-300 max-w-xl text-sm leading-relaxed mb-8">
          Welcome to your authenticated VaultFlow session. Phase 1 (Registration, Login, JWT Generation, Auto Refresh Tokens, BCrypt Security, and Protected Route Enforcement) is fully operational.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 w-full text-left">
          <div className="p-4 rounded-xl bg-slate-900/50 border border-slate-800">
            <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">User ID</h3>
            <p className="text-xs font-mono text-indigo-300 truncate">{user?.id}</p>
          </div>

          <div className="p-4 rounded-xl bg-slate-900/50 border border-slate-800">
            <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Email Address</h3>
            <p className="text-sm font-semibold text-slate-200 truncate">{user?.email}</p>
          </div>

          <div className="p-4 rounded-xl bg-slate-900/50 border border-slate-800">
            <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">Next Phase</h3>
            <p className="text-sm font-medium text-emerald-400 flex items-center gap-1.5">
              <Folder className="w-4 h-4" />
              Phase 2: File System
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};
