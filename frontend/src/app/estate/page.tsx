"use client";

import {SetStateAction, useEffect, useState} from "react";
import {searchEstates} from "@/api/estate";
import {Estate, EstateRegion} from "@/types/estate";
import {searchEstateRegions} from "@/api/estateRegion";

export default function EstatePage() {
    const [sido, setSido] = useState("");
    const [signguNm, setSignguNm] = useState("");
    const [keyword, setKeyword] = useState("");
    const [estates, setEstates] = useState<Estate[]>([]);
    const [estateRegions, setEstateRegions] = useState<EstateRegion[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSearch = async () => {
        // if (!sido || !signguNm) {
        //     alert("시/도와 시/군/구를 모두 입력해주세요.");
        //     return;
        // }

        // 전체 검색을 했다면 검색 우선
        // 전체 검색이 아니라면 selectbox사용
        //TODO: 띄어쓰기로 구분 . 추후 리팩토링

        let trimmedKeyword = keyword.trim();
        let searchKeyword = trimmedKeyword ? trimmedKeyword : sido + " " + signguNm;

        setLoading(true);
        setError(null);
        try {
            const response = await searchEstates({searchKeyword});
            setEstates(response.estateList);
            //전체검색시 selectItem 초기화
            if(trimmedKeyword){
                setKeyword("");
                setSignguNm("");
                setSido("");
            //키워드 검색시 keyword 초기화
            }else{
                setKeyword("");
            }


        } catch (err: any) {
            setError(err.message || "검색 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        // 내부에서 비동기 함수 정의
        const fetchRegions = async () => {
            try {
                setLoading(true); // 로딩 시작
                const response = await searchEstateRegions();
                setEstateRegions(response.estateRegionList || response);
            } catch (err : any) {
                setError(err.message || "지역을 받아오는 중 오류가 발생했습니다.");
            } finally {
                setLoading(false);
            }
        };
        fetchRegions(); // 호출
    }, []); // 마운트 시 1회 실행


    const handleSidoChange = (e: { target: { value: SetStateAction<string>; }; }) => {
      setSido(e.target.value);
      setSignguNm(""); // 시군구 초기화
    };

    const sidoList = estateRegions.filter(r => r.level === 1);
    const gunguList = estateRegions.filter(r => r.level === 2 && r.parentName === sido);

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };

  return (
    <main className="ds-page">
      <h1 className="ds-title">행복주택 검색</h1>

      <div className="ds-search-bar">
        <div className="ds-form-group">
          <label className="ds-label">시/도</label>
          {/*<input*/}
          {/*  type="text"*/}
          {/*  className="ds-input ds-input-inline"*/}
          {/*  placeholder="예: 서울특별시"*/}
          {/*  value={sido}*/}
          {/*  onChange={(e) => setSido(e.target.value)}*/}
          {/*  style={{ width: "180px" }}*/}
          {/*/>*/}
            <select
                className="ds-input ds-input-inline"
                value={sido}
                onChange={handleSidoChange}
                style={{ width: "180px" }}
            >
                <option value="">전체</option>
                {sidoList.map(region => (
                    <option key={region.name} value={region.name}>{region.name}</option>
                ))}
            </select>

        </div>
        <div className="ds-form-group">
          <label className="ds-label">시/군/구</label>
          {/*<input*/}
          {/*  type="text"*/}
          {/*  className="ds-input ds-input-inline"*/}
          {/*  placeholder="예: 강남구"*/}
          {/*  value={signguNm}*/}
          {/*  onChange={(e) => setSignguNm(e.target.value)}*/}
          {/*  style={{ width: "180px" }}*/}
          {/*/>*/}
            <select
                className="ds-input ds-input-inline"
                value={signguNm}
                onChange={(e) => setSignguNm(e.target.value)}
                style={{ width: "180px" }}
                disabled={!sido} // 시/도를 먼저 선택해야 활성화
            >
                <option value="">전체</option>
                {gunguList.map(region => (
                    <option key={region.name} value={region.name}>{region.name}</option>
                ))}
            </select>
        </div>
          <div className="ds-form-group">
              <label className="ds-label"> 전체검색 </label>
              <input
                type="text"
                className="ds-input ds-input-inline"
                placeholder="예: 서울시 강남구"
                value={keyword}
                onKeyDown={handleKeyDown}
                onChange={(e) => setKeyword(e.target.value)}
                style={{ width: "180px" }}
              />

          </div>
        <button onClick={handleSearch} disabled={loading} className="ds-btn ds-btn-primary">
          {loading ? "검색 중..." : "검색"}
        </button>
      </div>

      {error && <div className="ds-alert-error">{error}</div>}

      <h2 className="ds-subtitle">검색 결과 ({estates.length}건)</h2>

      {estates.length === 0 ? (
        <div className="ds-empty">검색 결과가 없습니다.</div>
      ) : (
        <div>
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
                <a href={estate.url} target="_blank" rel="noopener noreferrer">
                  상세 공고 보기
                </a>
              )}
            </div>
          ))}
        </div>
      )}
    </main>
  );
}
