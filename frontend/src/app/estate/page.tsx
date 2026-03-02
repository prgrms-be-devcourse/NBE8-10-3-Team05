"use client";

import React, { useEffect, useState } from "react";
import { searchEstates } from "@/api/estate";
import { Estate, EstateRegion } from "@/types/estate";
import { searchEstateRegions } from "@/api/estateRegion";

export default function EstatePage() {
  // 1. 상태 관리
  const [sido, setSido] = useState("");
  const [signguNm, setSignguNm] = useState("");
  const [keyword, setKeyword] = useState("");
  const [estates, setEstates] = useState<Estate[]>([]);
  const [estateRegions, setEstateRegions] = useState<EstateRegion[]>([]);

  // 페이지네이션 관련 상태
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalCount, setTotalCount] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false); // 검색 수행 여부

  // 2. 핵심 검색 로직
  const handleSearch = async (targetPage = 0) => {
    const trimmedKeyword = keyword.trim();
    const searchKeyword = trimmedKeyword ? trimmedKeyword : (sido + " " + signguNm).trim();

    setLoading(true);
    setError(null);
    try {
      const response = await searchEstates({
        searchKeyword,
        page: targetPage,
        size: 10
      });

      setEstates(response.estateList);
      setTotalPages(response.totalPages);
      setTotalCount(response.totalCount);
      setPage(response.currentPage);
      setHasSearched(true);

      // 페이지 이동 시 사용자 편의를 위해 상단으로 스크롤
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err: any) {
      setError(err.message || "검색 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 3. 지역 데이터 로드 (마운트 시)
  useEffect(() => {
    const fetchRegions = async () => {
      try {
        setLoading(true);
        const response = await searchEstateRegions();
        setEstateRegions(response.estateRegionList || response);
      } catch (err: any) {
        setError("지역 정보를 불러오는데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };
    fetchRegions();
  }, []);

  // 4. 이벤트 핸들러
  const handleSidoChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSido(e.target.value);
    setSignguNm("");
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch(0); // 엔터 시 1페이지부터 검색
    }
  };

  const sidoList = estateRegions.filter(r => r.level === 1);
  const gunguList = estateRegions.filter(r => r.level === 2 && r.parentName === sido);

  return (
    <main className="ds-page">
      <h1 className="ds-title">행복주택 검색</h1>

      {/* 검색 바 영역 */}
      <div className="ds-search-bar">
        <div className="ds-form-group">
          <label className="ds-label">시/도</label>
          <select className="ds-input ds-input-inline" value={sido} onChange={handleSidoChange} style={{ width: "150px" }}>
            <option value="">전체</option>
            {sidoList.map(region => (
              <option key={region.name} value={region.name}>{region.name}</option>
            ))}
          </select>
        </div>

        <div className="ds-form-group">
          <label className="ds-label">시/군/구</label>
          <select
            className="ds-input ds-input-inline"
            value={signguNm}
            onChange={(e) => setSignguNm(e.target.value)}
            style={{ width: "150px" }}
            disabled={!sido}
          >
            <option value="">전체</option>
            {gunguList.map(region => (
              <option key={region.name} value={region.name}>{region.name}</option>
            ))}
          </select>
        </div>

        <div className="ds-form-group">
          <label className="ds-label">전체검색</label>
          <input
            type="text"
            className="ds-input ds-input-inline"
            placeholder="예: 서울시 강남구"
            value={keyword}
            onKeyDown={handleKeyDown}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: "200px" }}
          />
        </div>

        <button onClick={() => handleSearch(0)} disabled={loading} className="ds-btn ds-btn-primary">
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div className="ds-alert-error">{error}</div>}

      {/* 결과 영역 */}
      {hasSearched && (
        <>
          <h2 className="ds-subtitle">검색 결과 ({totalCount.toLocaleString()}건)</h2>

          {estates.length === 0 ? (
            <div className="ds-empty">검색 결과가 없습니다.</div>
          ) : (
            <>
              <div className="ds-list-container">
                {estates.map((estate) => (
                  <div key={estate.id} className="ds-list-card">
                    <h3>{estate.pblancNm}</h3>
                    <div className="ds-list-grid">
                      <p><strong>상태:</strong> {estate.sttusNm}</p>
                      <p><strong>공급기관:</strong> {estate.suplyInsttNm}</p>
                      <p><strong>유형:</strong> {estate.suplyTyNm} ({estate.houseTyNm})</p>
                      <p><strong>주소:</strong> {estate.fullAdres}</p>
                      <p><strong>모집일:</strong> {estate.rcritPblancDe} ~ {estate.endDe}</p>
                      <p><strong>임대료:</strong> 보증금 {estate.rentGtn?.toLocaleString()}원 / 월 {estate.mtRntchrg?.toLocaleString()}원</p>
                    </div>
                    {estate.url && (
                      <a href={estate.url} target="_blank" rel="noopener noreferrer" className="ds-link">
                        상세 공고 보기
                      </a>
                    )}
                  </div>
                ))}
              </div>

              {/* 페이지네이션 버튼 */}
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
