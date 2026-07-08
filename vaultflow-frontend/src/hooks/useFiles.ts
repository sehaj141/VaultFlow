import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { filesApi } from "../api/filesApi";

export function useFiles(folderId: string | null) {
  const queryClient = useQueryClient();

  const filesQuery = useQuery({
    queryKey: ["files", folderId],
    queryFn: () => filesApi.list(folderId).then((res) => res.data),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["files", folderId] });

  const uploadFile = useMutation({
    mutationFn: ({ file, onProgress }: { file: File; onProgress?: (pct: number) => void }) =>
      filesApi.upload(folderId, file, onProgress),
    onSuccess: invalidate,
  });

  const deleteFile = useMutation({
    mutationFn: (fileId: string) => filesApi.remove(fileId),
    onSuccess: invalidate,
  });

  const renameFile = useMutation({
    mutationFn: ({ id, name }: { id: string; name: string }) => filesApi.rename(id, name),
    onSuccess: invalidate,
  });

  const downloadFile = async (fileId: string, fileName: string) => {
    const { data } = await filesApi.getDownloadUrl(fileId);
    const link = document.createElement("a");
    link.href = data.url;
    link.download = fileName;
    link.click();
  };

  return { filesQuery, uploadFile, deleteFile, renameFile, downloadFile };
}