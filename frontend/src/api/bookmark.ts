import type { BookmarkPolicyResponse, BookmarkUpdateResponse } from "@/types/policy";
import {fetchWithAuth} from "@/api/apiAuth";

// GET /api/v1/member/bookmark/welfare-bookmarks
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function getBookmarks(): Promise<BookmarkPolicyResponse> {
  const response = await fetchWithAuth(`/api/v1/member/bookmark/welfare-bookmarks`, {
    method: "GET"
  });

  const data: BookmarkPolicyResponse = await response.json();
  return data;
}

// POST /api/v1/member/bookmark/welfare-bookmarks/{policyId}
// 북마크 토글 (추가/제거)
// 인증 필요: 쿠키 기반 (credentials: "include")
export async function toggleBookmark(policyId: number): Promise<BookmarkUpdateResponse> {
  const response = await fetchWithAuth(
    `/api/v1/member/bookmark/welfare-bookmarks/${policyId}`,
    {
      method: "POST",
    }
  );

  if (!response.ok) {
    throw new Error(`북마크 요청 실패: ${response.status}`);
  }

  const data: BookmarkUpdateResponse = await response.json();
  return data;
}
