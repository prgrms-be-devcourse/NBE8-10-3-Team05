"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";
import { getMemberDetail } from "@/api/member";

// 저장할 사용자 정보
interface User {
  name: string;
  memberId?: number;
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  loginUser: (user: User) => void;
  logoutUser: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const STORAGE_KEY = "auth_user";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    const initAuth = async () => {

        const currentPath = window.location.pathname;
        const isGuestPage = ["/login", "/join"].includes(currentPath);
        const stored = typeof window !== "undefined" ? localStorage.getItem(STORAGE_KEY) : null;

        // 이미 로그인한 사용자가 로그인/회원가입 페이지에 들어온 경우만 메인으로!
        if (stored && isGuestPage) {
            window.location.href = "/";
            return;
        }

      try {
        // 1) localStorage 기반 복원
        const stored =
          typeof window !== "undefined"
            ? localStorage.getItem(STORAGE_KEY)
            : null;

        if (stored) {
          try {
            const parsed: User = JSON.parse(stored);
            if (!cancelled) {
              setUser(parsed);
            }
              // 이미 로그인 정보를 복원했고, 로그인 페이지에 있다면 메인으로 리다이렉트
              if(isGuestPage){
                  window.location.href = "/";
                  return;
              }

          } catch {
            if (!cancelled && typeof window !== "undefined") {
              localStorage.removeItem(STORAGE_KEY);
            }
          }
        }

        if (!isGuestPage) {
            // 2) 서버 세션 기반 복원 (소셜 로그인 포함)
            try {
                const detail = await getMemberDetail();
                if (!cancelled && detail) {
                    const serverUser: User = {
                        name: detail.name,
                        // memberId는 MemberDetailRes 계약에 없으므로 설정하지 않음
                    };
                    setUser(serverUser);
                    if (typeof window !== "undefined") {
                        localStorage.setItem(STORAGE_KEY, JSON.stringify(serverUser));
                    }
                }
            } catch {
                if (!cancelled) {
                    setUser(null);
                    // 유령 데이터 삭제
                    localStorage.removeItem(STORAGE_KEY);
                }
            }
        }

      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }



    };

    void initAuth();

    return () => {
      cancelled = true;
    };
  }, []);

  // 로그인 시 호출
  const loginUser = (userData: User) => {
    setUser(userData);
    if (typeof window !== "undefined") {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(userData));
    }
  };

  // 로그아웃 시 호출
  const logoutUser = () => {
    setUser(null);
    if (typeof window !== "undefined") {
      localStorage.removeItem(STORAGE_KEY);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, loginUser, logoutUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
