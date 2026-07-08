import { useQuery } from "@tanstack/react-query";
import { foldersApi } from "../api/foldersApi";

interface Props {
  folderId: string | null;
  onNavigate: (folderId: string | null) => void;
}

export default function Breadcrumb({ folderId, onNavigate }: Props) {
  const { data: trail } = useQuery({
    queryKey: ["breadcrumb", folderId],
    queryFn: () => foldersApi.breadcrumb(folderId!).then((res) => res.data),
    enabled: !!folderId,
  });

  return (
    <div className="flex items-center gap-2 text-sm text-slate-600 mb-4">
      <button onClick={() => onNavigate(null)} className="hover:text-indigo-600 font-medium">
        My Drive
      </button>
      {trail?.map((crumb) => (
        <span key={crumb.id} className="flex items-center gap-2">
          <span className="text-slate-400">/</span>
          <button onClick={() => onNavigate(crumb.id)} className="hover:text-indigo-600">
            {crumb.name}
          </button>
        </span>
      ))}
    </div>
  );
}