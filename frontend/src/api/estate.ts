import { EstateSearchRequest, EstateSearchResponse } from "@/types/estate";
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
 * 행복주택 검색
 * GET /api/v1/welfare/estate/location
 */
export async function searchEstates(
  req: EstateSearchRequest
): Promise<EstateSearchResponse> {
  const params = new URLSearchParams({
    // sido: req.sido,
    // signguNm: req.signguNm,
      keyword: req.searchKeyword
  });

  const response = await fetchWithAuth(
    `/api/v1/welfare/estate/location?${params.toString()}`,
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
