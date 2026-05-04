# Account Service

## 역할
사용자의 자산(Asset)과 잔액(Balance)을 관리하며, 주문 처리를 위한 자금 예약(Reservation) 및 실제 체결에 따른 정산(Settlement)을 담당합니다.

## 핵심 기술 및 패턴
- **Double-Entry Bookkeeping (복식 부기)**: 모든 잔액 변경은 `ledger_entry` 테이블에 기록되어 데이터의 무결성과 추적성을 보장합니다.
- **Reservation Mechanism**: 주문 시 필요한 자금을 즉시 차감하는 대신 `PENDING` 상태로 예약하여, 이중 지불을 방지하고 유연한 취소/체결 처리를 지원합니다.
- **Transactional Outbox Pattern**: 자산 처리 결과(성공/실패)를 Kafka로 안전하게 발행합니다.
- **JOOQ**: 타입 세이프한 SQL 쿼리 및 데이터 접근.

## 자산 처리 흐름 (Asset Flow)
1. **자금 예약 (Reserve)**:
   - `order-service`로부터 Kafka(`reserve-order`) 메시지를 수신.
   - 가용 잔액 확인 후, 해당 금액을 `available`에서 `reserved`로 이동시키고 `reservation` 기록 생성.
   - 성공 시 `AccountReservedEvent`, 실패 시 `AccountRejectedEvent` 발행.
2. **정산 (Settle)**:
   - `matching-engine`으로부터 `EngineMatchedEvent` 수신.
   - 예약된 자금(`reserved`)을 실제 차감하고, 매수자의 경우 획득한 자산을 잔액에 추가.
   - 체결 금액만큼 `reservation` 수량 업데이트.
3. **해제 (Release)**:
   - 주문 취소 또는 거절 시 `EngineCanceledEvent` / `EngineRejectedEvent` 수신.
   - 남은 예약 자금(`reserved`)을 다시 가용 잔액(`available`)으로 복구하고 `reservation` 종료.

## 주요 테이블
- `account`: 사용자 계정 정보.
- `asset`: 거래 가능한 자산 종류 (BTC, KRW 등).
- `balance`: 사용자별/자산별 현재 잔액 (Available, Reserved).
- `reservation`: 주문별 자금 예약 현황.
- `ledger_entry`: 모든 자산 변경 이력 (Audit log).
- `account_outbox`: Kafka 발행 대기 메시지.

## 에러 처리
- **Retryable Topic**: Kafka 컨슈머 실패 시 재시도 (Exponential Backoff).
- **DLQ Handler**: 최종 실패 시 `engine_failed_event` 테이블에 기록하여 데이터 불일치 조사 및 복구 지원.
- **Optimistic Locking**: 잔액 업데이트 시 버전 관리를 통해 동시성 제어.
