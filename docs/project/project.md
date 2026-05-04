# Project: Exchange (거래소)

## 개요
마이크로서비스 아키텍처 기반의 고성능 가상자산 거래소 플랫폼.

## 기술 스택
- **언어**: Java 25, Rust (2024 edition)
- **프레임워크**: Spring Boot 4.0.5 (Java), `rdkafka` / `tokio` / `crossbeam` (Rust)
- **데이터베이스**: PostgreSQL (JOOQ 사용)
- **메시징**: Apache Kafka 4.0 (이벤트 기반 매칭)
- **통신**: gRPC (동기), Kafka (비동기)

## 아키텍처
- **패턴**: 헥사고날 아키텍처 (Ports & Adapters)
- **구조**:
    - `services/`: 마이크로서비스 (Order, Account, Matching Engine)
    - `libs/`: 공용 모듈 (Core, Core-Web, Proto)

## 주요 서비스 (상세 정보 링크)
- **[`order-service`](order-service.md)** (Java): 주문 수명 주기 관리. 주문 검증 및 등록.
- **[`account-service`](account-service.md)** (Java): 자산 및 잔액 관리. gRPC 기반 자금 예약 수행.
- **[`matching-engine`](matching-engine.md)** (Rust): Kafka 이벤트를 통한 고속 주문 매칭 및 결과 생성.

## 통신 흐름
1. `order-service` -> `account-service` (gRPC): 주문을 위한 자금 예약.
2. `order-service` -> Kafka: 주문 이벤트 발행.
3. `matching-engine` -> Kafka: 주문 소비, 매칭 수행, 체결 결과 발행.
4. `order-service` / `account-service` -> Kafka: 체결 결과 소비하여 상태 및 잔액 업데이트.
