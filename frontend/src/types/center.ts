import { Page } from './common';

/**
 * API: GET /api/v1/welfare/center/location
 * Request Params: sido, signguNm
 */
export interface CenterSearchRequest {
  keyword: string;
  page?: number;
  size?: number;
}

export interface Center {
  id: number;
  location: string;
  name: string;
  address: string;
  contact: string;
  operator: string;
  corpType: string;
}

export interface CenterSearchResponse {
  centerList: Center[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
}

/**
 * API: GET /api/v1/welfare/center/location/lawyer
 * Request Params: LawyerReq + Pageable
 */
export interface LawyerSearchRequest {
  area1: string; // 시/도
  area2?: string; // 군/구
  page?: number;
  size?: number;
  sort?: string[];
}

export interface LawyerResponse {
  id: number;
  name: string;
  corporation: string;
  districtArea1: string;
  districtArea2: string;
}

export type LawyerPageResponse = Page<LawyerResponse>;
