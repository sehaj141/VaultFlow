interface Props {
    label: string;
    value: number | string;
    icon: string;
  }
  
  export default function StatCard({ label, value, icon }: Props) {
    return (
      <div className="bg-white rounded-xl p-6 shadow-sm border border-slate-100 flex items-center gap-4">
        <div className="text-3xl">{icon}</div>
        <div>
          <p className="text-2xl font-semibold text-slate-800">{value}</p>
          <p className="text-sm text-slate-500">{label}</p>
        </div>
      </div>
    );
  }