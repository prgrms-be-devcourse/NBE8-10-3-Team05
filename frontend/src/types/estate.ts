export interface Estate {
  id: number;
  pblancId: string;
  pblancNm: string;
  sttusNm: string;
  rcritPblancDe: string;
  beginDe: string;
  endDe: string;
  suplyHoCo: string;
  houseSn: number;
  suplyInsttNm: string;
  houseTyNm: string;
  suplyTyNm: string;
  hsmpNm: string;
  brtcNm: string;
  signguNm: string;
  signguCode: string;
  fullAdres: string;
  rentGtn: number;
  mtRntchrg: number;
  url: string;
}

export interface EstateRegion {
    name: string;
    parentName: string;
    level: number;
}

export interface EstateRegionResponse {
    estateRegionList : EstateRegion[]
}

export interface EstateSearchRequest {
  page?: number;  // 현재 몇 페이지인지 (0부터 시작)
  size?: number;  // 한 페이지에 몇 개를 보여줄 것인지
  searchKeyword: string;
}

export interface EstateSearchResponse {
  estateList: Estate[];
  totalCount: number;  // 전체 아이템 개수 (화면에 "총 n건" 표시용)
  totalPages: number;   // 전체 페이지 수 (페이지네이션 범위용)
  currentPage: number;  // 현재 페이지 번호 (0부터 시작)
}
