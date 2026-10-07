# 테이블 명세서

모든 테이블은 `BaseEntity`(JPA Auditing)를 상속해 `created_at`, `updated_at`을 자동 기록한다.
JPA가 만든 실제 테이블은 `\d 테이블명`으로 확인해 이 명세와 비교한다.

## users (회원)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, 자동 증가(IDENTITY) | 회원 번호 |
| `username` | VARCHAR(20) | NOT NULL, UNIQUE | 로그인 아이디 (4~20자) |
| `password` | VARCHAR(100) | NOT NULL | BCrypt 해시 (평문 저장 금지, 응답 노출 금지) |
| `role` | VARCHAR(20) | NOT NULL | `CUSTOMER` 또는 `OWNER` (EnumType.STRING) |
| `created_at` | TIMESTAMP | NOT NULL, 수정 불가 | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL | 수정 시각 |

## menus (메뉴)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, 자동 증가 | 메뉴 번호 |
| `owner_id` | BIGINT | FK → `users(id)`, NOT NULL | 메뉴 주인(사장님). `@ManyToOne(LAZY)` |
| `name` | VARCHAR(100) | NOT NULL | 메뉴 이름 |
| `price` | INTEGER | NOT NULL | 가격 (1원 이상) |
| `description` | VARCHAR(500) | NULL 허용 | 설명 (선택) |
| `deleted_at` | TIMESTAMP | NULL 허용 | Soft Delete. NULL = 정상, 값 있음 = 삭제됨 |
| `created_at` | TIMESTAMP | NOT NULL, 수정 불가 | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL | 수정 시각 |

## orders (주문)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, 자동 증가 | 주문 번호 |
| `customer_id` | BIGINT | FK → `users(id)`, NOT NULL | 주문자(손님). `@ManyToOne(LAZY)` |
| `menu_id` | BIGINT | FK → `menus(id)`, NOT NULL | 주문한 메뉴. `@ManyToOne(LAZY)` |
| `quantity` | INTEGER | NOT NULL | 수량 (1 이상) |
| `total_price` | INTEGER | NOT NULL | 총액 = 주문 시점 메뉴 가격 × 수량 (서버 계산) |
| `delivery_address` | VARCHAR(255) | NOT NULL | 배송 주소 |
| `status` | VARCHAR(20) | NOT NULL | `ORDERED` / `PAID` / `ACCEPTED` / `COMPLETED` / `CANCELED` |
| `created_at` | TIMESTAMP | NOT NULL, 수정 불가 | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL | 수정 시각 |

### 주문 상태 전이

| 현재 상태 | 다음 상태 | 누가 | 어떤 기능으로 |
| --- | --- | --- | --- |
| `ORDERED` | `PAID` | CUSTOMER | 결제 |
| `ORDERED` | `CANCELED` | CUSTOMER | 주문 취소 |
| `PAID` | `ACCEPTED` | OWNER | 상태 변경 |
| `ACCEPTED` | `COMPLETED` | OWNER | 상태 변경 |

위 표에 없는 전이는 모두 거절한다(409).

## payments (결제)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT | PK, 자동 증가 | 결제 번호 |
| `order_id` | BIGINT | FK → `orders(id)`, NOT NULL | 결제 대상 주문. `@ManyToOne(LAZY)` |
| `amount` | INTEGER | NOT NULL | 결제 금액 = 주문 `total_price` (서버가 복사) |
| `method` | VARCHAR(20) | NOT NULL | `CARD`만 허용 |
| `status` | VARCHAR(20) | NOT NULL | `COMPLETED` / `CANCELED` |
| `created_at` | TIMESTAMP | NOT NULL, 수정 불가 | 생성 시각 |
| `updated_at` | TIMESTAMP | NOT NULL | 수정 시각 |

## 엔티티 패키지 / 클래스 매핑

| 테이블 | 엔티티 | enum |
| --- | --- | --- |
| `users` | `user.entity.User` | `Role` |
| `menus` | `menu.entity.Menu` | — |
| `orders` | `order.entity.Order` | `OrderStatus` |
| `payments` | `payment.entity.Payment` | `PaymentMethod`, `PaymentStatus` |

기본 패키지: `com.sparta.delivery`
