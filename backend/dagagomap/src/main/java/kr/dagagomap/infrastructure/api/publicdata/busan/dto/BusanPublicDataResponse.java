package kr.dagagomap.infrastructure.api.publicdata.busan.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import kr.dagagomap.entity.Company;

/**
 * 참여업체명, 우대 내용 등 부산광역시 가족사랑카드참여업체 현황 조회 정보
 *
 * @param header 응답 관련 정보
 * @param body 응답 결과
 */
@JsonRootName("response")
public record BusanPublicDataResponse(
		/* 응답 관련 정보 */
		Header header,
		/* 응답 결과 */
		Body body
) {

	/**
	 * 응답 관련 정보
	 *
	 * @param resultCode 결과코드
	 * @param resultMsg 결과메시지
	 */
	public static record Header(
			/* 결과코드 */
			Integer resultCode,
			/* 결과메시지 */
			String resultMsg
	) {}

	/**
	 * 응답 결과
	 *
	 * @param totalCount 전체 결과 수
	 * @param numOfRows 한 페이지 결과 수
	 * @param pageNo 페이지 번호
	 * @param items 업체 정보들
	 */
	public static record Body(
			/* 전체 결과 수 */
			Integer totalCount,
			/* 한 페이지 결과 수 */
			Integer numOfRows,
			/* 페이지 번호 */
			Integer pageNo,
			/* 업체 정보들 */
			Item[] items
	) {

		/**
		 * 업체 정보
		 *
		 * @param cpCompname 참여업체명
		 * @param cpHome URL
		 * @param cpClass 업종코드
		 * @param cpHgu 지역
		 * @param cpCeoname 대표자명
		 * @param cpSanum 사업자번호
		 * @param cpSidate 시행일
		 * @param cpAddr 주소
		 * @param cpTel 연락처
		 * @param cpEmail 이메일
		 * @param cpEmailflag 이메일 수신여부
		 * @param cpInfo 회사소개
		 * @param cpWoo 우대 내용
		 * @param cpState 사용여부
		 * @param cpImg 첨부파일
		 * @param cpWebflag 승인여부
		 */
		public static record Item(
				/* 참여업체명 */
				String cpCompname,
				/* URL */
				String cpHome,
				/* 업종코드 */
				String cpClass,
				/* 지역 */
				String cpHgu,
				/* 대표자명 */
				String cpCeoname,
				/* 사업자번호 */
				String cpSanum,
				/* 시행일 */
				String cpSidate,
				/* 주소 */
				String cpAddr,
				/* 연락처 */
				String cpTel,
				/* 이메일 */
				String cpEmail,
				/* 이메일 수신여부 */
				String cpEmailflag,
				/* 회사소개 */
				String cpInfo,
				/* 우대 내용 */
				String cpWoo,
				/* 사용여부 */
				String cpState,
				/* 첨부파일 */
				String cpImg,
				/* 승인여부 */
				String cpWebflag
		) {

			public Company toCompany() {
				return Company.builder()
						.taxId(Long.valueOf(cpSanum))
						.name(cpCompname)
						.homepageUrl(cpHome)
						.category(cpClass)
						.gu(cpHgu)
						.ceoName(cpCeoname)
						.beginDate(cpSidate)
						.sourceAddress(cpAddr)
						.tel(cpTel)
						.email(cpEmail)
						.emailFlag(cpEmailflag)
						.description(cpInfo)
						.benefit(cpWoo)
						.usageStatus(cpState)
						.img(cpImg)
						.webFlag(cpWebflag)
						.build();
			}

		}

	}

}
