import axiosInstance from "./axiosInstance";
import { SearchParams, SearchResponse } from "../types/search.types";

export const searchApi = {
  search: (params: SearchParams) =>
    axiosInstance.get<SearchResponse>("/search", { params }),
};