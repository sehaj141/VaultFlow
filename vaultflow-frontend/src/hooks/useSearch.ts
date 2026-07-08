import { useQuery } from "@tanstack/react-query";
import { searchApi } from "../api/searchApi";
import { SearchParams } from "../types/search.types";

export function useSearch(params: SearchParams, enabled: boolean) {
  return useQuery({
    queryKey: ["search", params],
    queryFn: () => searchApi.search(params).then((res) => res.data),
    enabled, // don't fire on every keystroke — we debounce before setting enabled
    staleTime: 10_000,
  });
}