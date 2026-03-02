"use client";

import { useState } from "react";
import { searchCenters } from "@/api/center";
import { Center } from "@/types/center";

export default function CenterSearchPage() {
  const [keyword, setKeyword] = useState(""); // 입력창 하나로 통합
  const [centers, setCenters] = useState<Center[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!keyword.trim()) {
      alert("검색어를 입력해주세요.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await searchCenters({ keyword: keyword.trim() });
      setCenters(response.centerList);
    } catch (err: any) {
      setError(err.message || "검색 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="ds-page">
      <h1 className="ds-title">복지 센터 검색</h1>

      <div className="ds-search-bar">
        <div className="ds-form-group">
          <label className="ds-label">시/도</label>
          <input
            type="text"
            className="ds-input ds-input-inline"
            placeholder="예: 서울 종로"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: "180px" }}
          />
        </div>

        <button onClick={handleSearch} disabled={loading} className="ds-btn ds-btn-primary">
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div className="ds-alert-error">{error}</div>}

      <h2 className="ds-subtitle">검색 결과 ({centers.length}건)</h2>

      {centers.length === 0 ? (
        <div className="ds-empty">검색 결과가 없습니다.</div>
      ) : (
        <div>
          {centers.map((center) => (
            <div key={center.id} className="ds-list-card">
              <h3>{center.name}</h3>
              <p><strong>주소:</strong> {center.address}</p>
              <p><strong>연락처:</strong> {center.contact}</p>
              <p><strong>운영자:</strong> {center.operator}</p>
              <p><strong>유형:</strong> {center.corpType}</p>
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
