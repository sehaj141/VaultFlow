import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { foldersApi } from "../api/foldersApi";

export function useFolders(parentId: string | null) {
  const queryClient = useQueryClient();

  const foldersQuery = useQuery({
    queryKey: ["folders", parentId],
    queryFn: () => foldersApi.list(parentId).then((res) => res.data),
  });

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ["folders", parentId] });

  const createFolder = useMutation({
    mutationFn: (name: string) => foldersApi.create({ name, parentId }),
    onSuccess: invalidate,
  });

  const renameFolder = useMutation({
    mutationFn: ({ id, name }: { id: string; name: string }) =>
      foldersApi.rename(id, name),
    onSuccess: invalidate,
  });

  const moveFolder = useMutation({
    mutationFn: ({ id, newParentId }: { id: string; newParentId: string | null }) =>
      foldersApi.move(id, newParentId),
    onSuccess: invalidate,
  });

  const deleteFolder = useMutation({
    mutationFn: (id: string) => foldersApi.remove(id),
    onSuccess: invalidate,
  });

  return { foldersQuery, createFolder, renameFolder, moveFolder, deleteFolder };
}