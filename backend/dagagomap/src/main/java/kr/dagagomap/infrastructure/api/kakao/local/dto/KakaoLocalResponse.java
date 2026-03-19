package kr.dagagomap.infrastructure.api.kakao.local.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * 주소를 좌표로 변환하는 Kakao Local API 응답
 * 
 * @param meta 응답 관련 정보
 * @param document 응답 결과
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoLocalResponse(
		/** 응답 관련 정보 */
		Meta meta,
		/** 응답 결과 */
		Document[] documents
) {
	
	/**
	 * 응답 관련 정보
	 * 
	 * @param totalCount 검색어에 검색된 문서 수
	 * @param pageableCount totalCount 중 노출 가능 문서 수
	 * @param isEnd 현재 페이지가 마지막 페이지인지 여부
	 */
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static record Meta(
			/** 검색어에 검색된 문서 수 */
			Integer totalCount,
			/** totalCount 중 노출 가능 문서 수 */
			Integer pageableCount,
			/** 현재 페이지가 마지막 페이지인지 여부 */
			Boolean isEnd
	) {}

	/**
	 * 응답 결과
	 * 
	 * @param addressName 전체 지번 주소 또는 전체 도로명 주소, 입력에 따라 결정됨
	 * @param addressType <code>address_name</code>의 값의 타입(Type).
	 *   <code>REGION</code>(지명), <code>ROAD</code>(도로명), <code>REGION_ADDR</code>(지번 주소),
	 *   <code>ROAD_ADDR</code>(도로명 주소) 중 하나
	 * @param x X 좌표값, 경위도인 경우 경도(longitude)
	 * @param y Y 좌표값, 경위도인 경우 위도(latitude)
	 * @param address 지번 주소 상세 정보
	 * @param roadAddress 도로명 주소 상세 정보
	 */
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static record Document(
			/** 전체 지번 주소 또는 전체 도로명 주소, 입력에 따라 결정됨 */
			String addressName,
			/**
			 * <code>address_name</code>의 값의 타입(Type).
			 * <code>REGION</code>(지명), <code>ROAD</code>(도로명), <code>REGION_ADDR</code>(지번 주소),
			 * <code>ROAD_ADDR</code>(도로명 주소) 중 하나
			 */
			String addressType,
			/** X 좌표값, 경위도인 경우 경도(longitude) */
			String x,
			/** Y 좌표값, 경위도인 경우 위도(latitude) */
			String y,
			/** 지번 주소 상세 정보 */
			Address address,
			/** 도로명 주소 상세 정보 */
			RoadAddress roadAddress
	) {
		
		/**
		 * 지번 주소 상세 정보
		 * 
		 * @param addressName 전체 지번 주소
		 * @param region1DepthName 지역 1 Depth, 시도 단위
		 * @param region2DepthName 지역 2 Depth, 구 단위
		 * @param region3DepthName 지역 3 Depth, 동 단위
		 * @param region3DepthHName 지역 3 Depth, 행정동 명칭
		 * @param hCode 행정 코드
		 * @param bCode 법정 코드
		 * @param mountainYn 산 여부, <code>Y</code> 또는 <code>N</code>
		 * @param mainAddressNo 지번 주번지
		 * @param subAddressNo 지번 부번지, 없을 경우 빈 문자열("") 반환
		 * @param x X 좌표값, 경위도인 경우 경도(longitude)
		 * @param y Y 좌표값, 경위도인 경우 위도(latitude)
		 */
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public static record Address(
				/** 전체 지번 주소 */
				String addressName,
				/** 지역 1 Depth, 시도 단위 */
				@JsonProperty("region_1depth_name")
				String region1DepthName,
				/** 지역 2 Depth, 구 단위 */
				@JsonProperty("region_2depth_name")
				String region2DepthName,
				/** 지역 3 Depth, 동 단위 */
				@JsonProperty("region_3depth_name")
				String region3DepthName,
				/** 지역 3 Depth, 행정동 명칭 */
				@JsonProperty("region_3depth_h_name")
				String region3DepthHName,
				/** 행정 코드 */
				String hCode,
				/** 법정 코드 */
				String bCode,
				/** 산 여부, <code>Y</code> 또는 <code>N</code> */
				String mountainYn,
				/** 지번 주번지 */
				String mainAddressNo,
				/** 지번 부번지, 없을 경우 빈 문자열("") 반환 */
				String subAddressNo,
				/** X 좌표값, 경위도인 경우 경도(longitude) */
				String x,
				/** Y 좌표값, 경위도인 경우 위도(latitude) */
				String y
		) {}
		
		/**
		 * 도로명 주소 상세 정보
		 * 
		 * @param addressName 전체 도로명 주소
		 * @param region1depthName 지역명1
		 * @param region2depthName 지역명2
		 * @param region3depthName 지역명3
		 * @param roadName 도로명
		 * @param undergroundYn 지하 여부, <code>Y</code> 또는 <code>N</code>
		 * @param mainBuildingNo 건물 본번
		 * @param subBuildingNo 건물 부번, 없을 경우 빈 문자열(<code>""</code>) 반환
		 * @param buildingName 건물 이름
		 * @param zoneNo 우편번호(5자리)
		 * @param x X 좌표값, 경위도인 경우 경도(longitude)
		 * @param y Y 좌표값, 경위도인 경우 위도(latitude)
		 */
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public static record RoadAddress(
				/** 전체 도로명 주소 */
				String addressName,
				/** 지역명1 */
				@JsonProperty("region_1depth_name")
				String region1DepthName,
				/** 지역명2 */
				@JsonProperty("region_2depth_name")
				String region2DepthName,
				/** 지역명3 */
				@JsonProperty("region_3depth_name")
				String region3DepthName,
				/** 도로명 */
				String roadName,
				/** 지하 여부, <code>Y</code> 또는 <code>N</code> */
				String undergroundYn,
				/** 건물 본번 */
				String mainBuildingNo,
				/** 건물 부번, 없을 경우 빈 문자열(<code>""</code>) 반환 */
				String subBuildingNo,
				/** 건물 이름 */
				String buildingName,
				/** 우편번호(5자리) */
				String zoneNo,
				/** X 좌표값, 경위도인 경우 경도(longitude) */
				String x,
				/** Y 좌표값, 경위도인 경우 위도(latitude) */
				String y
		) {}
		
	}

}
