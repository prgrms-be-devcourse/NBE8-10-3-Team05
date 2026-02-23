import {
  CenterSearchRequest,
  CenterSearchResponse,
  LawyerSearchRequest,
  LawyerPageResponse,
} from "../types/center";
import {fetchWithAuth} from "@/api/apiAuth";

export class ApiError extends Error {
  constructor(
    public resultCode: string,
    public msg: string
  ) {
    super(`${resultCode}: ${msg}`);
    this.name = "ApiError";
  }
}

/**
 * 복지 센터 검색
 * GET /api/v1/welfare/center/location
 */
export async function searchCenters(
  req: CenterSearchRequest
): Promise<CenterSearchResponse> {
  const params = new URLSearchParams({
    sido: req.sido,
    signguNm: req.signguNm,
  });

  const response = await fetchWithAuth(
    `/api/v1/welfare/center/location?${params.toString()}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      }
    }
  );

  if (!response.ok) {
    const errorData = await response.json();
    throw new ApiError(errorData.resultCode || "UNKNOWN", errorData.msg || "Unknown error");
  }

  return response.json();
}

/**
 * 법률 상담소 검색
 * GET /api/v1/welfare/center/location/lawyer
 */
export async function searchLawyers(
  req: LawyerSearchRequest
): Promise<LawyerPageResponse> {
  const params = new URLSearchParams();
  params.append("area1", req.area1);
  if (req.area2) params.append("area2", req.area2);
  if (req.page !== undefined) params.append("page", req.page.toString());
  if (req.size !== undefined) params.append("size", req.size.toString());
  if (req.sort) {
    req.sort.forEach((s) => params.append("sort", s));
  }

  const response = await fetchWithAuth(
    `/api/v1/welfare/center/location/lawyer?${params.toString()}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      }
    }
  );

  if (!response.ok) {
    const errorData = await response.json();
    throw new ApiError(errorData.resultCode || "UNKNOWN", errorData.msg || "Unknown error");
  }

  return response.json();
}
