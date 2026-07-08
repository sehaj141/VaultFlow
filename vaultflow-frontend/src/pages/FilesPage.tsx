import { useState } from "react";
import { useFolders } from "../hooks/useFolders";
import FolderGrid from "../components/FolderGrid";
import Breadcrumb from "../components/Breadcrumb";
import CreateFolderModal from "../components/CreateFolderModal";

export default function FilesPage() {
  const [currentFolderId, setCurrentFolderId] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  const { foldersQuery, createFolder } = useFolders(currentFolderId);

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-4">
        <Breadcrumb folderId={currentFolderId} onNavigate={setCurrentFolderId} />
        <button
          onClick={() => setModalOpen(true)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-indigo-700"
        >
          + New Folder
        </button>
      </div>

      {foldersQuery.isLoading ? (
        <p className="text-slate-400 text-sm">Loading...</p>
      ) : (
        <FolderGrid
          folders={foldersQuery.data ?? []}
          parentId={currentFolderId}
          onOpen={setCurrentFolderId}
        />
      )}

      <CreateFolderModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onCreate={(name) => {
          createFolder.mutate(name);
          setModalOpen(false);
        }}
        isPending={createFolder.isPending}
      />
    </div>
  );
}