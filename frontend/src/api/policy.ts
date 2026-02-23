import type { PolicyDocument, PolicySearchRequest } from "@/types/policy";
import type { ApiErrorResponse } from "@/types/member";
import { ApiError } from "@/api/member";
import {fetchWithAuth} from "@/api/apiAuth";

// GET /api/v1/welfare/policy/search
export async function searchPolicies(
  params: PolicySearchRequest
): Promise<PolicyDocument[]> {
  const query = new URLSearchParams();

  // 필수 파라미터
  query.set("from", String(params.from));
  query.set("size", String(params.size));

  // 선택 파라미터
  if (params.keyword) query.set("keyword", params.keyword);
  if (params.age != null) query.set("age", String(params.age));
  if (params.earn != null) query.set("earn", String(params.earn));
  if (params.regionCode) query.set("regionCode", params.regionCode);
  if (params.jobCode) query.set("jobCode", params.jobCode);
  if (params.schoolCode) query.set("schoolCode", params.schoolCode);
  if (params.marriageStatus) query.set("marriageStatus", params.marriageStatus);
  if (params.zipCd) query.set("zipCd", params.zipCd);

  // keywords는 배열 → 반복 파라미터로 전달
  if (params.keywords && params.keywords.length > 0) {
    for (const kw of params.keywords) {
      query.append("keywords", kw);
    }
  }

  const queryString = query.toString();
  const url = `/api/v1/welfare/policy/search${queryString ? `?${queryString}` : ""}`;

  const response = await fetchWithAuth(url, {
    method: "GET"
  });

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: PolicyDocument[] = await response.json();
  return data;
}
