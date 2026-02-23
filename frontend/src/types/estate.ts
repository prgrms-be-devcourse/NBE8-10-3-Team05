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
  // sido: string;
  // signguNm: string;
    searchKeyword: string;
}

export interface EstateSearchResponse {
  estateList: Estate[];
}
