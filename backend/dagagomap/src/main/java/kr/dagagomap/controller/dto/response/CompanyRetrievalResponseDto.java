package kr.dagagomap.controller.dto.response;

import kr.dagagomap.entity.Company;
import lombok.Getter;

@Getter
public class CompanyRetrievalResponseDto {

	private final String taxId; // 사업자번호
	private final String name; // 참여업체명
	private final String homepageUrl; // URL
	private final String category; // 업종코드
	private final String gu; // 지역(구)
	private final String ceoName; // 대표자명
	private final String beginDate; // 시행일
	private final String sourceAddress; // 원본 주소
	private final String normalizedAddress; // 좌표 변환용으로 전처리한 주소
	private final String tel; // 연락처
	private final String email; // 이메일
	private final String emailFlag; // 이메일 수신여부
	private final String description; // 회사소개
	private final String benefit; // 우대 내용
	private final String usageStatus; // 사용여부
	private final String img; // 첨부파일
	private final String webFlag; // 승인여부
	private final Double latitude; // 위도
	private final Double longitude; // 경도

	public CompanyRetrievalResponseDto(Company company) {
		this.taxId = company.getTaxId();
		this.name = company.getName();
		this.homepageUrl = company.getHomepageUrl();
		this.category = company.getCategory();
		this.gu = company.getGu();
		this.ceoName = company.getCeoName();
		this.beginDate = company.getBeginDate();
		this.sourceAddress = company.getSourceAddress();
		this.normalizedAddress = company.getNormalizedAddress();
		this.tel = company.getTel();
		this.email = company.getEmail();
		this.emailFlag = company.getEmailFlag();
		this.description = company.getDescription();
		this.benefit = company.getNormalizedBenefit();
		this.usageStatus = company.getUsageStatus();
		this.img = company.getImg();
		this.webFlag = company.getWebFlag();
		this.latitude = company.getLatitude();
		this.longitude = company.getLongitude();
	}

}
