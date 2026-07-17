package kr.dagagomap.infrastructure.api.kakao.local.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * 주소를 좌표로 변환하는 Kakao Local API 응답
 *
 * @param meta 응답 관련 정보
 * @param documents 응답 결과
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PlaceSearchByKeywordResponse(
		/* 응답 관련 정보 */
		Meta meta,
		/* 응답 결과 */
		Document[] documents
) {

	/**
	 * 응답 관련 정보
	 *
	 * @param totalCount 검색어에 검색된 문서 수
	 * @param pageableCount totalCount 중 노출 가능 문서 수
	 * @param isEnd 현재 페이지가 마지막 페이지인지 여부
	 * @param sameName 질의어의 지역 및 키워드 분석 정보
	 */
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static record Meta(
			/* 검색어에 검색된 문서 수 */
			Integer totalCount,
			/* totalCount 중 노출 가능 문서 수 */
			Integer pageableCount,
			/* 현재 페이지가 마지막 페이지인지 여부 */
			Boolean isEnd,
			/* 질의어의 지역 및 키워드 분석 정보 */
			SameName sameName
	) {}

	/**
	 * 질의어의 지역 및 키워드 분석 정보
	 *
	 * @param region 질의어에서 인식된 지역의 리스트. 예: '중앙로 맛집' 에서 중앙로에 해당하는 지역 리스트
	 * @param keyword 질의어에서 지역 정보를 제외한 키워드. 예: '중앙로 맛집' 에서 '맛집'
	 * @param selectedREgion 인식된 지역 리스트 중, 현재 검색에 사용된 지역 정보
	 */
	public static record SameName(
			/* 질의어에서 인식된 지역의 리스트. 예: '중앙로 맛집' 에서 중앙로에 해당하는 지역 리스트 */
			String[] region,
			/* 질의어에서 지역 정보를 제외한 키워드. 예: '중앙로 맛집' 에서 '맛집' */
			String keyword,
			/* 인식된 지역 리스트 중, 현재 검색에 사용된 지역 정보 */
			String selectedRegion
	) {}

	/**
	 * 응답 결과
	 *
	 * @param id 장소 ID
	 * @param placeName 장소명, 업체명
	 * @param categoryName 카테고리 이름
	 * @param categoryGroupCode 중요 카테고리만 그룹핑한 카테고리 그룹 코드
	 * @param categoryGroupName 중요 카테고리만 그룹핑한 카테고리 그룹명
	 * @param phone 전화번호
	 * @param addressName 전체 지번 주소
	 * @param roadAddressName 전체 도로명 주소
	 * @param x X 좌표값, 경위도인 경우 longitude (경도)
	 * @param y Y 좌표값, 경위도인 경우 latitude(위도)
	 * @param placeUrl 장소 상세페이지 URL
	 * @param distance 중심좌표까지의 거리 (단, x,y 파라미터를 준 경우에만 존재) 단위 meter
	 */
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static record Document(
			String id,
			String placeName,
			String categoryName,
			String categoryGroupCode,
			String categoryGroupName,
			String phone,
			String addressName,
			String roadAddressName,
			String x,
			String y,
			String placeUrl,
			String distance
	) {}

}
