const STORAGE_KEY = "auth_user";

export function isAuthRequired(url: string): boolean {
    // 1. API 요청이 아니면 무조건 통과 (모든 페이지 노출)
    const isApiRequest = url.startsWith("/api") || url.includes(window.location.origin + "/api");
    if (!isApiRequest) return false;

    // 2. 로그인이 반드시 필요한 API 목록 (신청, 북마크, 로그아웃 등)
    const AUTH_REQUIRED_PATHS = [
        "/api/v1/member/member/logout",
        "/api/v1/member/bookmark/welfare-bookmarks",
        "/api/v1/member/policy-aply/welfare-applications"
    ];

    // 현재 요청이 위 목록에 포함되는지 확인
    const needsLogin = AUTH_REQUIRED_PATHS.some(path => url.includes(path));

    // [중요] 목록에 없는 API(조회 등)는 로그인 여부 상관없이 무조건 통과!
    if (!needsLogin) {
        return false;
    }

    // 3. 목록에 있는 API인데 로컬스토리지에 정보가 없다면? 인증 필요(true) 반환
    const hasAuthInfo = typeof window !== "undefined" && !!localStorage.getItem(STORAGE_KEY);
    return !hasAuthInfo;
}

async function reissue(): Promise<boolean> {
    const response = await fetch(`/api/v1/auth/reissue`, {
        method: "POST",
        credentials: "include",
    });
    return response.ok;
}

export async function fetchWithAuth(
    url: string,
    options: RequestInit
): Promise<Response> {
    if (isAuthRequired(url)) {
        console.warn("[fetchWithAuth] 인증이 필요한 API입니다. 로그인으로 이동합니다.");
        if (typeof window !== "undefined") {
            window.location.href = "/login";
        }
        return Promise.reject("Redirecting to login...");
    }

    const response = await fetch(url, {
        ...options,
        credentials: "include",
    });

    if (response.status === 401) {
        console.log("[fetchWithAuth] 401 감지, reissue 시도...");
        try {
            const reissued = await reissue();
            console.log("[fetchWithAuth] reissue 결과:", reissued);
            if (reissued) {
                console.log("[fetchWithAuth] 원래 요청 재시도...");
                return await fetch(url, {
                    ...options,
                    credentials: "include",
                });
            }else{
                //TODO: 중복로그인 혹은 refreshToken유효기간 만료 등
                throw new Error("reissue_failed");
            }
        } catch (reissueErr) {
            console.error("[fetchWithAuth] 인증 만료. 로그인 페이지로 이동합니다 : ", reissueErr);
            //cleanup auth비우기
            if (typeof window !== "undefined") {
                localStorage.removeItem(STORAGE_KEY);
                window.location.href = "/login";
            }
            return Promise.reject(reissueErr);
        }
    }

    return response;
}
