# API 명세서

## 공통 규칙

| 항목 | 내용 |
| --- | --- |
| Base URL | `http://localhost:8080` |
| 형식 | 요청·응답 모두 `application/json` |
| 인증 | 로그인 필요한 API는 `Authorization: Bearer {토큰}` 헤더 |
| 시각 형식 | `2026-10-07T14:05:00` (ISO-8601, 타임존 없음) |
| 금액 | 원 단위 정수 |

### RESTful URL 규칙

- 자원은 복수 명사: `/api/menus`, `/api/orders`
- 행위는 HTTP 메서드로 표현한다.
- 딸린 자원은 계층으로 표현한다: `/api/orders/{orderId}/payments`
- CRUD에 딱 맞지 않는 상태 변경은 **`PATCH` + 하위 경로**로 통일한다: `/cancel`, `/status`

### 상태 코드 정책

| 코드 | 사용처 |
| --- | --- |
| 200 | 조회·수정 성공, 로그인 성공 |
| 201 | 생성 성공 (회원가입, 메뉴 등록, 주문 생성, 결제) |
| 204 | 본문 없는 성공 (삭제, 취소, 상태 변경) |
| 400 | 입력값 검증 실패, 허용되지 않는 값(예: 카드 외 결제 수단) |
| 401 | 로그인 실패 (아이디 없음 / 비밀번호 불일치) |
| 403 | 역할 불일치, 남의 리소스 접근. 토큰 없음/무효도 기본 동작상 403 |
| 404 | 없는 리소스, 삭제된 메뉴 |
| 409 | 아이디 중복, **현재 상태와 맞지 않는 요청**(이미 결제됨, 취소 불가, 허용되지 않는 상태 전이) |

> 규칙 위반(상태 불일치)은 400이 아니라 **409로 통일**한다.

### 요청 처리 순서 (검증 우선순위)

여러 조건이 동시에 어긋날 때 아래 순서로 판단해 일관된 코드를 낸다.
**400(입력 검증) → 403(역할·소유자) → 404(대상 없음) → 409(상태 충돌)**
단, 소유자 판단에는 대상 조회가 필요하므로 실제 구현은 `대상 조회(404) → 소유자 확인(403) → 상태 확인(409)` 순서가 된다.

### 접근 권한 요약

| API | 비로그인 | CUSTOMER | OWNER |
| --- | --- | --- | --- |
| 회원가입 · 로그인 | O | O | O |
| 메뉴 목록 · 단건 조회 | O | O | O |
| 메뉴 등록 · 수정 · 삭제 | X | 403 | 본인 메뉴만 |
| 주문 생성 · 취소 · 결제 | X | 본인 것만 | 403 |
| 주문 목록 조회 | X | 내 주문 | 내 메뉴 주문 |
| 주문 상태 변경 | X | 403 | 내 메뉴 주문만 |

---

## 1. 회원

### 1-1. 회원가입

| 항목 | 내용 |
| --- | --- |
| Method · URL | `POST /api/users` |
| 권한 | 누구나 |
| 성공 | `201 Created` |
| 실패 | `400` 검증 실패 · `409` 아이디 중복 |

요청

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `username` | String | O | 4~20자 |
| `password` | String | O | 8자 이상 |
| `role` | String | O | `CUSTOMER` 또는 `OWNER` |

```json
{ "username": "owner1", "password": "password1234", "role": "OWNER" }
```

응답 `201` (비밀번호는 포함하지 않는다)

```json
{ "userId": 1, "username": "owner1", "role": "OWNER", "createdAt": "2026-10-07T14:05:00" }
```

### 1-2. 로그인

| 항목 | 내용 |
| --- | --- |
| Method · URL | `POST /api/auth/login` |
| 권한 | 누구나 |
| 성공 | `200 OK` |
| 실패 | `400` 값 누락 · `401` 아이디 없음 또는 비밀번호 불일치 |

요청

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `username` | String | O | 아이디 |
| `password` | String | O | 비밀번호 |

응답 `200` — JWT 클레임: 아이디(subject), 역할(`role`), 만료 시간(`exp`). 비밀번호는 담지 않는다.

```json
{ "accessToken": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer" }
```

---

## 2. 메뉴

메뉴 응답 공통 형태

```json
{
  "menuId": 1,
  "ownerId": 1,
  "name": "김밥",
  "price": 3000,
  "description": "참치김밥",
  "createdAt": "2026-10-07T14:05:00",
  "updatedAt": "2026-10-07T14:05:00"
}
```

### 2-1. 메뉴 등록

| 항목 | 내용 |
| --- | --- |
| Method · URL | `POST /api/menus` |
| 권한 | OWNER |
| 성공 | `201 Created` + 메뉴 응답 |
| 실패 | `400` 이름 비어있음·가격 1 미만 · `403` CUSTOMER · 토큰 없음 |

요청: `name`(String, 필수, 비어있지 않음) · `price`(Integer, 필수, 1 이상) · `description`(String, 선택)

```json
{ "name": "김밥", "price": 3000, "description": "참치김밥" }
```

메뉴 주인은 요청 본문이 아니라 **토큰의 사용자**다.

### 2-2. 메뉴 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method · URL | `GET /api/menus` |
| 권한 | 누구나 |
| 성공 | `200 OK` + 메뉴 응답 배열 (**삭제된 메뉴 제외**) |

### 2-3. 메뉴 단건 조회

| 항목 | 내용 |
| --- | --- |
| Method · URL | `GET /api/menus/{menuId}` |
| 권한 | 누구나 |
| 성공 | `200 OK` + 메뉴 응답 |
| 실패 | `404` 없거나 삭제된 메뉴 |

### 2-4. 메뉴 수정

| 항목 | 내용 |
| --- | --- |
| Method · URL | `PUT /api/menus/{menuId}` |
| 권한 | OWNER (본인 메뉴만) |
| 성공 | `200 OK` + 수정된 메뉴 응답 |
| 실패 | `400` 검증 실패 · `403` CUSTOMER 또는 타인 메뉴 · `404` 없거나 삭제된 메뉴 |

요청은 등록(2-1)과 같다. 이름·가격·설명을 통째로 교체하므로 `PUT`을 쓴다.

### 2-5. 메뉴 삭제 (Soft Delete)

| 항목 | 내용 |
| --- | --- |
| Method · URL | `DELETE /api/menus/{menuId}` |
| 권한 | OWNER (본인 메뉴만) |
| 성공 | `204 No Content` |
| 실패 | `403` CUSTOMER 또는 타인 메뉴 · `404` 없거나 이미 삭제된 메뉴 |

실제로 지우지 않고 `deleted_at`에 현재 시각을 기록한다. 삭제된 메뉴는 목록에서 빠지고, 단건 조회·수정·주문에서는 404로 처리한다. 이미 들어온 주문 기록은 그대로 남는다.

---

## 3. 주문

주문 응답 공통 형태

```json
{
  "orderId": 1,
  "menuId": 1,
  "menuName": "김밥",
  "customerId": 3,
  "quantity": 2,
  "totalPrice": 7000,
  "deliveryAddress": "서울시 강남구 테헤란로 1",
  "status": "ORDERED",
  "createdAt": "2026-10-07T14:10:00",
  "updatedAt": "2026-10-07T14:10:00"
}
```

### 3-1. 주문 생성

| 항목 | 내용 |
| --- | --- |
| Method · URL | `POST /api/orders` |
| 권한 | CUSTOMER |
| 성공 | `201 Created` + 주문 응답 (`status`: `ORDERED`) |
| 실패 | `400` 수량 1 미만·주소 없음 · `403` OWNER · `404` 없거나 삭제된 메뉴 |

요청: `menuId`(Long, 필수) · `quantity`(Integer, 필수, 1 이상) · `deliveryAddress`(String, 필수, 비어있지 않음)

```json
{ "menuId": 1, "quantity": 2, "deliveryAddress": "서울시 강남구 테헤란로 1" }
```

총액은 `메뉴 가격 × 수량`을 서버가 계산한다. 요청으로 금액을 받지 않는다. 주문자는 토큰에서 꺼낸다.

### 3-2. 주문 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method · URL | `GET /api/orders` |
| 권한 | 로그인한 사용자 |
| 성공 | `200 OK` + 주문 응답 배열 |

- CUSTOMER: **본인이 한 주문**만
- OWNER: **본인 메뉴에 들어온 주문**만 (삭제된 메뉴의 주문 포함)
- 없으면 빈 배열 `[]`

### 3-3. 주문 취소

| 항목 | 내용 |
| --- | --- |
| Method · URL | `PATCH /api/orders/{orderId}/cancel` |
| 권한 | CUSTOMER (본인 주문만) |
| 성공 | `204 No Content` (상태 → `CANCELED`) |
| 실패 | `403` OWNER 또는 타인 주문 · `404` 없는 주문 · `409` `ORDERED`가 아닌 주문 |

### 3-4. 주문 상태 변경

| 항목 | 내용 |
| --- | --- |
| Method · URL | `PATCH /api/orders/{orderId}/status` |
| 권한 | OWNER (본인 메뉴에 들어온 주문만) |
| 성공 | `204 No Content` |
| 실패 | `400` 허용되지 않는 값 · `403` CUSTOMER 또는 타인 메뉴 주문 · `404` 없는 주문 · `409` 허용되지 않는 전이 |

요청: `status`(String, 필수) — `ACCEPTED` 또는 `COMPLETED`

```json
{ "status": "ACCEPTED" }
```

허용 전이는 `PAID → ACCEPTED`, `ACCEPTED → COMPLETED` 두 가지뿐이다. 요청 `status`가 `PAID`·`CANCELED`·`ORDERED` 등이면 `400`, 값은 맞지만 현재 상태와 맞지 않으면 `409`.

---

## 4. 결제

### 4-1. 결제

| 항목 | 내용 |
| --- | --- |
| Method · URL | `POST /api/orders/{orderId}/payments` |
| 권한 | CUSTOMER (본인 주문만) |
| 성공 | `201 Created` + 결제 응답, 주문 상태 `ORDERED → PAID` |
| 실패 | `400` 카드 외 결제 수단 · `403` OWNER 또는 타인 주문 · `404` 없는 주문 · `409` `ORDERED`가 아닌 주문(이미 결제·취소) |

요청: `method`(String, 필수) — `CARD`만 허용

```json
{ "method": "CARD" }
```

응답 `201`

```json
{
  "paymentId": 1,
  "orderId": 1,
  "amount": 7000,
  "method": "CARD",
  "status": "COMPLETED",
  "createdAt": "2026-10-07T14:12:00"
}
```

결제 금액은 요청으로 받지 않고 **주문 총액을 서버가 그대로** 쓴다. 결제 저장과 주문 상태 변경은 **하나의 트랜잭션**으로 처리한다.

---

## 12개 필수 기능 ↔ API 대응표

| # | 기능 | Method · URL |
| --- | --- | --- |
| 1 | 회원가입 | `POST /api/users` |
| 2 | 로그인 | `POST /api/auth/login` |
| 3 | 메뉴 등록 | `POST /api/menus` |
| 4 | 메뉴 목록 조회 | `GET /api/menus` |
| 5 | 메뉴 단건 조회 | `GET /api/menus/{menuId}` |
| 6 | 메뉴 수정 | `PUT /api/menus/{menuId}` |
| 7 | 메뉴 삭제 | `DELETE /api/menus/{menuId}` |
| 8 | 주문 생성 | `POST /api/orders` |
| 9 | 주문 목록 조회 | `GET /api/orders` |
| 10 | 주문 취소 | `PATCH /api/orders/{orderId}/cancel` |
| 11 | 주문 상태 변경 | `PATCH /api/orders/{orderId}/status` |
| 12 | 결제 | `POST /api/orders/{orderId}/payments` |

## 보안 설정 메모 (SecurityConfig 구현 시 참고)

- `/error`는 `permitAll` (열지 않으면 400·404·409가 전부 빈 403으로 나간다)
- CSRF 비활성화 (JWT 방식, 안 끄면 POST·PATCH·DELETE가 403)
- `permitAll`: `POST /api/users`, `POST /api/auth/login`, `GET /api/menus/**`
- 그 외는 `authenticated()`, 역할 제한은 `hasRole`(또는 서비스 단 검증)
- 세션은 `STATELESS`, JWT 필터는 `UsernamePasswordAuthenticationFilter` 앞에 등록
