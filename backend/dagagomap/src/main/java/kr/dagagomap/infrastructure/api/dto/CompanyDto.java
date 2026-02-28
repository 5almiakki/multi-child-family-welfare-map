package kr.dagagomap.infrastructure.api.dto;

public record CompanyDto(
		Integer resultCode, // 결과코드
		String resultMsg, // 결과메시지
		Integer numOfRows, // 한 페이지 결과 수
		Integer pageNo, // 페이지 번호
		Integer totalCount, // 전체 결과 수
		String cpCompname, // 참여업체명
		String cpHome, // URL
		String cpClass, // 업종코드
		String cpHgu, // 지역
		String cpCeoname, // 대표자명
		String cpSanum, // 사업자번호
		String cpSidate, // 시행일
		String cpAddr, // 주소
		String cpTel, // 연락처
		String cpEmail, // 이메일
		String cpEmailflag, // 이메일 수신여부
		String cpInfo, // 회사소개
		String cpWoo, // 우대 내용
		String cpState, // 사용여부
		String cpImg, // 첨부파일
		String cpWebflag// 승인여부
) {

}
