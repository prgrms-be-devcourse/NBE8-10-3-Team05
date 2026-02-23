// Member API 호출 함수
import type {
  JoinRequest,
  JoinResponse,
  LoginRequest,
  LoginResponse,
  AddressDto,
  MemberDetailRes,
  MemberDetailReq,
  ApiErrorResponse,
} from "@/types/member";
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

// ===== 회원가입 (인증 불필요) =====
export async function join(request: JoinRequest): Promise<JoinResponse> {
  const response = await fetch(`/api/v1/member/member/join`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: JoinResponse = await response.json();
  return data;
}

// ===== 로그인 (인증 불필요) =====
export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`/api/v1/member/member/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: LoginResponse = await response.json();
  return data;
}

// ===== 주소 업데이트 (인증 필요) =====
export async function updateAddress(
  addressDto: AddressDto
): Promise<MemberDetailRes> {
  const response = await fetchWithAuth(
    `/api/v1/member/member/detail/address`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(addressDto),
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: MemberDetailRes = await response.json();
  return data;
}

// ===== 회원 상세 정보 수정 (인증 필요) =====
export async function updateMemberDetail(
  req: MemberDetailReq
): Promise<MemberDetailRes> {
  const response = await fetchWithAuth(
    `/api/v1/member/member/detail`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(req),
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: MemberDetailRes = await response.json();
  return data;
}

// ===== 회원 상세 정보 조회 (인증 필요) =====
export async function getMemberDetail(): Promise<MemberDetailRes> {
  const response = await fetchWithAuth(
    `/api/v1/member/member/detail`,
    {
      method: "GET",
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }

  const data: MemberDetailRes = await response.json();
  return data;
}

// ===== 로그아웃 (인증 필요) =====
export async function logout(): Promise<void> {
  const response = await fetchWithAuth(
    `/api/v1/member/member/logout`,
    {
      method: "POST",
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }
}

// ===== 소셜 가입 추가정보 입력 (인증 필요) =====
export async function completeSocialSignup(data: {
  rrnFront: string;
  rrnBackFirst: string;
}): Promise<void> {
  const response = await fetchWithAuth(
    `/api/v1/member/member/complete-social`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();
    throw new ApiError(errorData.resultCode, errorData.msg);
  }
}
