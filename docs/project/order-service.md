# Order Service

## 역할
주문의 전체 수명 주기를 관리하고, 가용한 자산 확인(Account Service) 및 매칭 엔진(Matching Engine)과의 연동을 담당합니다.

## 핵심 기술 및 패턴
- **Transactional Outbox Pattern**: 모든 외부 시스템(Kafka)으로의 메시지 발행은 `order_outbox` 테이블을 거쳐 보장됩니다.
- **Event-Driven Architecture**: 계좌 예약 결과 및 매칭 결과를 Kafka 이벤트를 통해 비동기로 처리합니다.
- **JOOQ**: 타입 세이프한 SQL 쿼리 및 데이터 접근.

## 주문 흐름 (Order Lifecycle)
1. **주문 생성 (INIT)**: API 수신 시 `order` 테이블에 `INIT` 상태로 저장 및 자산 예약을 위한 `order_outbox` 생성.
2. **자산 예약 (Account Service 연동)**: 
   - `account-service`가 Kafka 메시지 수신 후 자산 예약 수행.
   - `order-service`는 `AccountReservedEvent`를 수신하여 주문 상태를 `PENDING`으로 변경하고, 엔진 전달을 위한 `order_outbox` 생성.
3. **매칭 엔진 접수 (ACCEPTED)**:
   - `matching-engine`이 Kafka 메시지 수신 후 주문 접수.
   - `order-service`는 `EngineAcceptedEvent`를 수신하여 주문 상태를 `ACCEPTED`로 변경.
4. **체결 처리 (MATCHED)**:
   - `EngineMatchedEvent` 수신 시 `trade` 기록 및 주문의 체결 수량/잔액 업데이트.
   - 전량 체결 시 `FILLED` 상태로 변경.
5. **취소/거절 (CANCELED/REJECTED)**:
   - 엔진 또는 계좌 서비스에서 거절 시 해당 상태로 업데이트하고, 예약된 자산 해제를 위한 outbox 발행.

## 주요 테이블
- `order`: 주문 기본 정보 및 상태.
- `order_history`: 주문 상태 변경 이력.
- `order_outbox`: Kafka 발행 대기 메시지.
- `trade`: 체결 내역.
- `consumer_failed_event`: Kafka 컨슈머 실패 내역 (DLQ 처리용).

## 에러 처리
- **Retryable Topic**: Kafka 컨슈머 실패 시 3회 재시도 (Exponential Backoff).
- **DLQ Handler**: 최종 실패 시 `consumer_failed_event` 테이블에 기록하여 추후 복구 지원.
