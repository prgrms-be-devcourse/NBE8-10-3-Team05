"use client";

import { useState } from "react";
import { searchCenters } from "@/api/center";
import { Center } from "@/types/center";

export default function CenterSearchPage() {
  const [keyword, setKeyword] = useState(""); // 입력창 하나로 통합
  const [centers, setCenters] = useState<Center[]>([]);

  // 페이지네이션 상태 추가
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalCount, setTotalCount] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false);

  const handleSearch = async (targetPage = 0) => {
    const trimmedKeyword = keyword.trim();
    setLoading(true);
    setError(null);
    try {
      const response = await searchCenters({
        keyword: trimmedKeyword,
        page: targetPage,
        size: 10
      });

      // 백엔드 CenterSearchResponseDto 구조에 맞춰 매핑
      setCenters(response.centerList);
      setTotalPages(response.totalPages);
      setTotalCount(response.totalCount);
      setPage(response.currentPage);
      setHasSearched(true);

      // 페이지 이동 시 상단 스크롤
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (err: any) {
      setError(err.message || "검색 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSearch(0);
    }
  };

  return (
    <main className="ds-page">
      <h1 className="ds-title">복지 센터 검색</h1>

      <div className="ds-search-bar">
        <div className="ds-form-group">
          <label className="ds-label">지역/센터명 검색</label>
          <input
            type="text"
            className="ds-input ds-input-inline"
            placeholder="예: 서울 종로"
            value={keyword}
            onKeyDown={handleKeyDown}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: "250px" }}
          />
        </div>

        {/* 신규 검색은 항상 0페이지부터 */}
        <button onClick={() => handleSearch(0)} disabled={loading} className="ds-btn ds-btn-primary">
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div className="ds-alert-error">{error}</div>}

      {hasSearched && (
        <>
          <h2 className="ds-subtitle">검색 결과 ({totalCount.toLocaleString()}건)</h2>

          {centers.length === 0 ? (
            <div className="ds-empty">검색 결과가 없습니다.</div>
          ) : (
            <>
              <div className="ds-list-container">
                {centers.map((center) => (
                  <div key={center.id} className="ds-list-card">
                    <h3>{center.name}</h3>
                    <div className="ds-list-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                      <p><strong>주소:</strong> {center.address}</p>
                      <p><strong>연락처:</strong> {center.contact}</p>
                      <p><strong>운영자:</strong> {center.operator}</p>
                      <p><strong>유형:</strong> {center.corpType}</p>
                      <p><strong>지역:</strong> {center.location}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* 페이지네이션 UI */}
              <div className="ds-pagination" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '15px', marginTop: '30px' }}>
                <button
                  className="ds-btn"
                  disabled={page === 0 || loading}
                  onClick={() => handleSearch(page - 1)}
                >
                  이전
                </button>

                <span className="ds-page-info">
                  <strong>{page + 1}</strong> / {totalPages}
                </span>

                <button
                  className="ds-btn"
                  disabled={page >= totalPages - 1 || loading}
                  onClick={() => handleSearch(page + 1)}
                >
                  다음
                </button>
              </div>
            </>
          )}
        </>
      )}
    </main>
  );
}
