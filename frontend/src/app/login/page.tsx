"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { login, ApiError } from "@/api/member";
import { useAuth } from "@/contexts/AuthContext";
import type { LoginRequest } from "@/types/member";

export default function LoginPage() {
  const router = useRouter();
  const { loginUser } = useAuth();

  // 폼 상태
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // UI 상태
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const request: LoginRequest = {
      email,
      password,
    };

    try {
      const response = await login(request);
      // Context + localStorage에 저장
      loginUser({
        memberId: response.memberId,
        name: response.name,
      });
      // 메인 페이지로 이동
      router.push("/");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(`[${err.resultCode}] ${err.msg}`);
      } else {
        setError("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  // 소셜 로그인 핸들러
  const handleSocialLogin = (provider: "kakao" | "naver") => {
    // 백엔드 OAuth2 엔드포인트로 리다이렉트
    window.location.href = `/oauth2/authorization/${provider}`;
  };

  return (
    <main className="ds-page-narrow">
      <h1 className="ds-title">로그인</h1>

      {error && <div className="ds-alert-error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="ds-form-group">
          <label className="ds-label">이메일</label>
          <input
            type="email"
            className="ds-input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className="ds-form-group">
          <label className="ds-label">비밀번호</label>
          <input
            type="password"
            className="ds-input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="ds-btn ds-btn-primary ds-btn-full"
        >
          {isLoading ? "로그인 중..." : "로그인"}
        </button>
      </form>

      {/* 구분선 */}
      <div className="ds-divider">
        <span>또는</span>
      </div>

      {/* 소셜 로그인 버튼 */}
      <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
        <button
          onClick={() => handleSocialLogin("kakao")}
          className="ds-social-btn"
        >
          <img
            src="/images/kakao_login_medium_narrow.png"
            alt="카카오 로그인"
            style={{ width: "183px", height: "43px" }}
          />
        </button>
      </div>

      {/* 회원가입 링크 */}
      <div style={{ marginTop: "24px", textAlign: "center" }}>
        <span className="ds-meta">계정이 없으신가요? </span>
        <a href="/join" className="ds-link">회원가입</a>
      </div>
    </main>
  );
}
