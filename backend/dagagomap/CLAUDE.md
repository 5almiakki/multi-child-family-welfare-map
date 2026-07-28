# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 참고하는 프로젝트 가이드입니다.

## 프로젝트 개요

**부산광역시 다자녀가정 우대업체 지도 서비스**의 백엔드입니다.

- 공공데이터포털(data.go.kr) 제공하는 **다자녀가정 우대업체 현황** 데이터를 주기적으로 수집·저장합니다.
- 저장된 업체 정보(상호명, 주소, 업종, 우대 내용 등)를 **카카오맵(Kakao Maps) API**와 연동하여
  지도 위에 마커로 표시할 수 있도록 좌표 변환 및 조회 API를 제공합니다.
- 프런트엔드(웹/앱)는 이 백엔드가 제공하는 REST API를 호출해 지도 화면을 구성합니다.

---

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 4
- **Build Tool**: Gradle (Groovy DSL)
- **DB**: MySQL + JPA/Hibernate
- **좌표 변환/지오코딩**: 카카오 로컬 API (주소 → 좌표 변환, `주소 검색하기` API)
- **스케줄링**: Spring `@Scheduled`
- **HTTP 클라이언트**: `RestClient` (공공데이터/카카오 API 호출)
- **캐시**: Redis (지도 조회 성능 최적화)
- **테스트**: JUnit 5, Mockito, `@SpringBootTest`

---

## 디렉토리 구조

프로젝트명: **dagagomap** (베이스 패키지: `kr.dagagomap`)

```
backend/dagagomap
├── src/main/java/kr/dagagomap
│   ├── DagagomapApplication.java
│   ├── config/
│   ├── controller/
│   │   └── dto.response/
│   ├── entity/
│   ├── exception/
│   ├── infrastructure.api/
│   │   ├── kakao.local/
│   │   │   └── dto/
│   │   └── publicdata.busan/
│   │       └── dto/
│   ├── repository/
│   └── service/
│
├── src/main/resources
│
├── src/test/java/kr/dagagomap
│   ├── benchmark/
│   ├── integration/
│   ├── service/
│   ├── support/
│   └── DagagomapApplicationTests.java
│
└── src/test/resources
```

> 패키지/클래스 표기: 위 `infrastructure.api/kakao.local`, `infrastructure.api/publicdata.busan`, `controller/dto.response`는 실제로는 각각 `infrastructure.api.kakao.local`, `infrastructure.api.publicdata.busan`, `controller.dto.response` 형태의 하위 패키지입니다.

---

## 공공데이터 연동 (부산광역시 다자녀가정 우대업체)

1. ### 인증
- `application-secret.yml`에 서비스키(인증키)를 저장하고 **절대 커밋하지 않습니다.**

### 동기화 흐름

1. `BusinessSyncScheduler`가 주기(예: 매일 새벽 3시)로 실행
2. `BusanOpenDataClient`가 원본 API 호출 → `RawBusinessData` 리스트 획득
3. `OpenDataSyncService`가 원본 데이터를 정제
   - 주소만 있고 좌표가 없는 경우 `GeocodingService`(카카오 로컬 API)로 좌표 변환
   - 기존 데이터와 비교하여 신규/변경/삭제 처리 (업체명+주소를 유니크 키로 사용 권장)
4. 변환된 데이터를 `Business` 엔티티로 저장 (`BusinessRepository`)
5. 좌표 변환 실패(주소 오류 등) 건은 별도 로그/실패 목록으로 관리하고 서비스는 계속 진행

---

## 개발 규칙

- **패키지 구조**: `kr.dagagomap` 하위에 `controller / entity / exception / infrastructure.api / repository / service / config` 계층으로 구성. 계층 간 의존은 controller → service → repository
- **외부 연동 클라이언트 분리**: 카카오는 `infrastructure.api.kakao.local`,
  부산 공공데이터는 `infrastructure.api.publicdata.busan`으로 완전히 분리하고, 각 클라이언트의 원본 응답 DTO(`*Response`)는 해당 패키지의 `dto/` 하위에만 위치
- **엔티티 직접 노출 금지**: Controller는 Entity가 아닌 DTO만 반환
- **예외 처리**: `CompanyExceptionHandler`에서 공통 처리, 모든 커스텀 예외는 `BaseCustomException`을 상속하고, 외부 API 실패는 별도 커스텀 예외로 래핑
- **QueryDSL 사용 원칙**: 단순 CRUD는 `CompanyRepository`(Spring Data JPA), bounding box/반경/
- **좌표 컬럼**: `latitude`, `longitude` (Double), 검색 성능을 위해 공간 인덱스 또는 위경도 범위 인덱스 고려
- **테스트**: 외부 API(공공데이터, 카카오) 호출부는 단위 테스트에서 반드시 Mock 처리하고, `integration/UpdateCompaniesIntegrationTest`처럼 실제 흐름 검증이 필요한 경우에만 통합 테스트로 분리. 테스트용 공공데이터는 `support/PublicDataTestFixtures`를 재사용
- **커밋 전 확인**: `application-secret.yml`을 포함한 민감정보 파일이 Git에 포함되지 않았는지 확인. API 키, 서비스키가 코드/설정 파일에 하드코딩되지 않았는지 확인

---

## 빌드 & 실행

```bash
./gradlew build
./gradlew bootRun --args='--spring.profiles.active=local'
./gradlew test
```
