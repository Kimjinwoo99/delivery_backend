# 배달 주문 서비스 백엔드 API

사장님이 메뉴를 올리고, 손님이 주문·결제하고, 사장님이 주문을 받아 처리하는 작은 배달 주문 서비스의 백엔드 API입니다.
화면 없이 API만 제공하며 Postman 등으로 확인합니다.

```
회원·로그인 → 메뉴 → 주문 → 결제 → 주문 처리
```

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| 언어 · 프레임워크 | Java 21 · Spring Boot 4.1.1 |
| 빌드 | Gradle (Groovy) |
| 데이터베이스 | PostgreSQL 18 |
| 주요 라이브러리 | Spring Web · Spring Data JPA · Spring Security · Validation · Lombok · JJWT 0.12.6 |
| 구조 | 모놀리식 · 3 Layer (Controller – Service – Repository) |

## 실행 방법

### 1. PostgreSQL 준비 (Docker)

```bash
docker run --name delivery-db -e POSTGRES_USER=delivery -e POSTGRES_PASSWORD=delivery1234 -e POSTGRES_DB=delivery -p 15432:5432 -d postgres:18
```

이미 만든 컨테이너는 `docker start delivery-db`로 켭니다. 포트와 계정은 `src/main/resources/application.yaml`과 같아야 합니다.
위 DB 비밀번호는 내 PC에서만 쓰는 연습용 값입니다.

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

`http://localhost:8080`에서 실행됩니다. JPA `ddl-auto: update`로 테이블이 자동 생성됩니다.

### JWT 비밀키

`application.yaml`의 `jwt.secret`은 학습용 더미 값입니다. HS256은 **32바이트 이상**의 키가 필요하며, 실제로 쓸 키는 환경변수로 주입합니다.

```bash
JWT_SECRET=<32바이트-이상의-비밀키> ./gradlew bootRun
```

## 역할

| 역할 | 할 수 있는 것 |
| --- | --- |
| `CUSTOMER` (손님) | 메뉴 조회 · 주문 생성 · 주문 취소 · 결제 · 내 주문 조회 |
| `OWNER` (사장님) | 메뉴 등록·수정·삭제 · 내 메뉴에 들어온 주문 조회 · 주문 상태 변경 |

## API 요약

로그인이 필요한 요청은 `Authorization: Bearer {토큰}` 헤더를 보냅니다. 상세 명세는 [`docs/api.md`](docs/api.md)를 참고하세요.

| # | 기능 | Method · URL | 권한 | 성공 |
| --- | --- | --- | --- | --- |
| 1 | 회원가입 | `POST /api/users` | 누구나 | 201 |
| 2 | 로그인 | `POST /api/auth/login` | 누구나 | 200 |
| 3 | 메뉴 등록 | `POST /api/menus` | OWNER | 201 |
| 4 | 메뉴 목록 조회 | `GET /api/menus` | 누구나 | 200 |
| 5 | 메뉴 단건 조회 | `GET /api/menus/{menuId}` | 누구나 | 200 |
| 6 | 메뉴 수정 | `PUT /api/menus/{menuId}` | OWNER (본인 메뉴) | 200 |
| 7 | 메뉴 삭제 (Soft Delete) | `DELETE /api/menus/{menuId}` | OWNER (본인 메뉴) | 204 |
| 8 | 주문 생성 | `POST /api/orders` | CUSTOMER | 201 |
| 9 | 주문 목록 조회 | `GET /api/orders` | 로그인한 사용자 | 200 |
| 10 | 주문 취소 | `PATCH /api/orders/{orderId}/cancel` | CUSTOMER (본인 주문) | 204 |
| 11 | 주문 상태 변경 | `PATCH /api/orders/{orderId}/status` | OWNER (본인 메뉴 주문) | 204 |
| 12 | 결제 | `POST /api/orders/{orderId}/payments` | CUSTOMER (본인 주문) | 201 |

### 주문 상태 흐름

```
ORDERED ──(손님 결제)──▶ PAID ──(사장님 수락)──▶ ACCEPTED ──(사장님 완료)──▶ COMPLETED
   └──(손님 취소, 결제 전에만)──▶ CANCELED
```

표에 없는 상태 변경은 모두 거절합니다.

### 상태 코드 정책

| 코드 | 사용처 |
| --- | --- |
| 400 | 입력값 검증 실패, 허용되지 않는 값(예: 카드 외 결제 수단) |
| 401 | 로그인 실패 |
| 403 | 역할 불일치, 남의 리소스 접근 (토큰이 없거나 잘못된 경우도 Spring Security 기본 동작상 403) |
| 404 | 없는 리소스, 삭제된 메뉴 |
| 409 | 아이디 중복, **현재 상태와 맞지 않는 요청**(이미 결제됨, 취소 불가, 허용되지 않는 상태 전이) |

## 프로젝트 구조

도메인별로 먼저 나누고, 그 안을 계층별로 나눴습니다.

```
src/main/java/com/sparta/delivery
├── global
│   ├── config      SecurityConfig, JpaAuditingConfig
│   ├── security    JwtUtil, JwtAuthenticationFilter, AuthUser
│   └── entity      BaseEntity (생성·수정 시각, JPA Auditing)
├── user            controller · service · repository · entity · dto
├── menu            controller · service · repository · entity · dto
├── order           controller · service · repository · entity · dto
├── payment         controller · service · repository · entity · dto
└── DeliveryApplication.java
```

## 설계 문서

| 문서 | 내용 |
| --- | --- |
| [`docs/erd.md`](docs/erd.md) | ERD와 설계 결정 |
| [`docs/tables.md`](docs/tables.md) | 테이블 명세서, 주문 상태 전이표 |
| [`docs/api.md`](docs/api.md) | API 명세서 |

## 주요 설계 포인트

- **테이블 이름**: `user`, `order`는 PostgreSQL 예약어라 `users`, `orders` 등 복수형으로 통일했습니다.
- **Soft Delete**: 메뉴는 `deleted_at`만 기록하고 행은 남깁니다. 삭제된 메뉴는 목록에서 빠지고 단건 조회·수정·주문에서는 404로 처리하지만, 이미 들어온 주문 기록은 그대로 보입니다.
- **금액은 서버가 계산**: 총액은 `메뉴 가격 × 수량`, 결제 금액은 주문 총액을 서버가 사용합니다. 요청에 금액을 보내도 무시합니다.
- **소유자 확인**: 메뉴 주인과 주문자는 요청 본문이 아니라 JWT에서 꺼냅니다. 남의 메뉴·주문은 403입니다.
- **동시 결제 방지**: 결제 시 주문 행에 쓰기 잠금(`PESSIMISTIC_WRITE`)을 걸고, 결제 저장과 주문 상태 변경을 한 트랜잭션으로 처리합니다. 같은 주문에 결제 요청이 동시에 와도 결제 완료는 한 번만 생깁니다.
- **회원 중복 가입**: 서비스에서 검사하고, 동시 요청은 DB `unique` 제약이 막아 409로 응답합니다.
- **로그인 실패**: 아이디 없음과 비밀번호 불일치를 구분하지 않고 같은 401로 응답합니다.
- **N+1 방지**: 주문 목록은 `@EntityGraph`로 메뉴를 함께 조회합니다.
- **보안 설정**: `/error`를 열어 두어 400·404·409가 빈 403으로 바뀌지 않게 했고, JWT 방식이라 CSRF와 세션은 사용하지 않습니다.

## 테스트 시나리오

과제의 필수 기능 12개를 Postman에서 순서대로 확인할 수 있는 시나리오는 과제 문서 5-1을 따릅니다. 요청 본문에 한글이 들어가면 UTF-8로 보내야 합니다.
