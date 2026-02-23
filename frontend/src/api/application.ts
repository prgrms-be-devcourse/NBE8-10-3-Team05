import type {
  ApplicationItem,
  AddApplicationResponse,
  DeleteApplicationResponse,
} from "@/types/policy";
import {fetchWithAuth} from "@/api/apiAuth";

// GET /api/v1/member/policy-aply/welfare-applications
// 신청 내역 조회
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function getApplications(): Promise<ApplicationItem[]> {
  const response = await fetchWithAuth(
    `/api/v1/member/policy-aply/welfare-applications`,
    {
      method: "GET",
    }
  );

  if (!response.ok) {
    throw new Error(`신청 내역 조회 실패: ${response.status}`);
  }

  const data: ApplicationItem[] = await response.json();
  return data;
}

// POST /api/v1/member/policy-aply/welfare-application/{policyId}
// 정책 신청
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function addApplication(
  policyId: number
): Promise<AddApplicationResponse> {
  const response = await fetchWithAuth(
    `/api/v1/member/policy-aply/welfare-application/${policyId}`,
    {
      method: "POST",
    }
  );

  const data: AddApplicationResponse = await response.json();

  if (!response.ok) {
    throw new Error(data.message || `신청 실패: ${response.status}`);
  }

  return data;
}

// PUT /api/v1/member/policy-aply/welfare-application/{id}
// 신청 취소 (id = Application.id)
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function deleteApplication(
  applicationId: number
): Promise<DeleteApplicationResponse> {
  const response = await fetchWithAuth(
    `/api/v1/member/policy-aply/welfare-application/${applicationId}`,
    {
      method: "PUT",
    }
  );

  const data: DeleteApplicationResponse = await response.json();

  if (!response.ok) {
    throw new Error(data.message || `신청 취소 실패: ${response.status}`);
  }

  return data;
}
