import { useQuery } from "@tanstack/react-query";
import { dashboardApi } from "../api/dashboardApi";

export function useDashboard() {
  return useQuery({
    queryKey: ["dashboard"],
    queryFn: () => dashboardApi.get().then((res) => res.data),
    staleTime: 30_000, // dashboard data doesn't need to be second-fresh
  });
}