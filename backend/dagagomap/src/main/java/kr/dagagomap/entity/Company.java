package kr.dagagomap.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
		name = "company",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_company_name_source_address",
				columnNames = {"name", "source_address"}
		)
)
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String taxId; // 사업자번호
	private String name; // 참여업체명
	private String homepageUrl; // URL
	private String category; // 업종코드
	private String gu; // 지역(구)
	private String ceoName; // 대표자명
	private String beginDate; // 시행일
	private String sourceAddress; // 원본 주소
	private String normalizedAddress; // 좌표 변환용으로 전처리한 주소
	private String tel; // 연락처
	private String email; // 이메일
	private String emailFlag; // 이메일 수신여부
	@Column(columnDefinition = "TEXT")
	private String description; // 회사소개
	private String benefit; // 우대 내용
	private String usageStatus; // 사용여부
	private String img; // 첨부파일
	private String webFlag; // 승인여부
	private Boolean coordinatesUpdateRequired;
	private Double latitude; // 위도
	private Double longitude; // 경도

	@Builder
	public Company(
			String taxId, String name, String homepageUrl, String category, String gu, String ceoName, String beginDate,
			String sourceAddress, String normalizedAddress, String tel, String email, String emailFlag,
			String description, String benefit, String usageStatus, String img, String webFlag) {
		this.taxId = taxId;
		this.name = name;
		this.homepageUrl = homepageUrl;
		this.category = category;
		this.gu = gu;
		this.ceoName = ceoName;
		this.beginDate = beginDate;
		this.sourceAddress = sourceAddress;
		this.normalizedAddress = normalizedAddress;
		this.tel = tel;
		this.email = email;
		this.emailFlag = emailFlag;
		this.description = description;
		this.benefit = benefit;
		this.usageStatus = usageStatus;
		this.img = img;
		this.webFlag = webFlag;
	}

	public void updateCoordinates(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void updateWithoutCoordinates(
			String taxId, String name, String homepageUrl, String category, String gu, String ceoName, String beginDate,
			String sourceAddress, String tel, String email, String emailFlag, String description, String benefit,
			String usageStatus, String img, String webFlag) {
		this.taxId = taxId;
		this.name = name;
		this.homepageUrl = homepageUrl;
		this.category = category;
		this.gu = gu;
		this.ceoName = ceoName;
		this.beginDate = beginDate;
		this.sourceAddress = sourceAddress;
		this.tel = tel;
		this.email = email;
		this.emailFlag = emailFlag;
		this.description = description;
		this.benefit = benefit;
		this.usageStatus = usageStatus;
		this.img = img;
		this.webFlag = webFlag;
	}

	public void updateCoordinatesUpdateRequired(Boolean coordinatesUpdateRequired) {
		this.coordinatesUpdateRequired = coordinatesUpdateRequired;
	}

	public record NaturalKey(String name, String sourceAddress) {}

	public NaturalKey naturalKey() {
		return new NaturalKey(name, sourceAddress);
	}

}
