import { useRef, useState, DragEvent } from "react";

interface Props {
  onUpload: (file: File, onProgress: (pct: number) => void) => void;
}

export default function UploadDropzone({ onUpload }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [progress, setProgress] = useState<number | null>(null);

  const handleFiles = (files: FileList | null) => {
    if (!files || files.length === 0) return;
    const file = files[0];
    setProgress(0);
    onUpload(file, (pct) => {
      setProgress(pct);
      if (pct === 100) setTimeout(() => setProgress(null), 600);
    });
  };

  return (
    <div
      onDragOver={(e: DragEvent) => {
        e.preventDefault();
        setIsDragging(true);
      }}
      onDragLeave={() => setIsDragging(false)}
      onDrop={(e: DragEvent) => {
        e.preventDefault();
        setIsDragging(false);
        handleFiles(e.dataTransfer.files);
      }}
      onClick={() => inputRef.current?.click()}
      className={`border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition ${
        isDragging ? "border-indigo-500 bg-indigo-50" : "border-slate-300 hover:border-slate-400"
      }`}
    >
      <input
        ref={inputRef}
        type="file"
        hidden
        onChange={(e) => handleFiles(e.target.files)}
        accept=".pdf,.docx,.txt,.zip,.png,.jpg,.jpeg"
      />
      {progress !== null ? (
        <div className="space-y-2">
          <div className="w-full bg-slate-200 rounded-full h-2">
            <div
              className="bg-indigo-600 h-2 rounded-full transition-all"
              style={{ width: `${progress}%` }}
            />
          </div>
          <p className="text-sm text-slate-500">{progress}% uploaded</p>
        </div>
      ) : (
        <p className="text-slate-500 text-sm">
          Drag & drop a file here, or click to browse
          <br />
          <span className="text-xs text-slate-400">PDF, DOCX, TXT, ZIP, PNG, JPEG — max 50MB</span>
        </p>
      )}
    </div>
  );
}