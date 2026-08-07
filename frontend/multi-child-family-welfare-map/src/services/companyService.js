import api from "@/services/api";

/**
 * @typedef {Object} Company
 * @property {number} taxId 사업자번호
 * @property {string} name 참여업체명
 * @property {string} homepageUrl URL
 * @property {string} category 업종코드
 * @property {string} gu 지역(구)
 * @property {string} ceoName 대표자명
 * @property {string} beginDate 시행일
 * @property {string} sourceAddress 원본 주소
 * @property {string} normalizedAddress 좌표 변환용으로 전처리한 주소
 * @property {string} tel 연락처
 * @property {string} email 이메일
 * @property {string} emailFlag 이메일 수신여부
 * @property {string} description 회사소개
 * @property {string} benefit 우대 내용
 * @property {string} usageStatus 사용여부
 * @property {string} img 첨부파일
 * @property {string} webFlag 승인여부
 * @property {number} latitude 위도
 * @property {number} longitude 경도
 */

/**
 * @typedef {Object} CompanySearchParams
 * @property {string} [name] 업체명 검색어
 * @property {string[]} [gus] 지역(구) 필터
 * @property {string[]} [categories] 업종 필터
 * @property {number} latitude 현재 위도
 * @property {number} longitude 현재 경도
 */

/**
 * 조건에 맞는 업체 목록을 조회한다.
 * @param {CompanySearchParams} params
 * @param {AbortSignal} [signal] 요청 취소용 signal
 * @returns {Promise<Company[]>}
 */
export const searchCompanies = async (params, signal) => {
  const { data } = await api.get("/api/v1/companies", { params, signal });
  return data;
};

/**
 * 업체명 자동완성 목록을 조회한다.
 * @param {string} keyword 검색어
 * @param {number} [limit=10] 최대 결과 수
 * @param {AbortSignal} [signal] 요청 취소용 signal
 * @returns {Promise<string[]>}
 */
export const autocompleteCompanyNames = async (keyword, limit = 10, signal) => {
  const { data } = await api.get("/api/v1/companies/autocomplete", {
    params: { keyword, limit },
    signal
  });
  return data;
};
