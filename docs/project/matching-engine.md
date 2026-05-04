# Matching Engine

## 역할
거래소의 핵심 로직인 주문 매칭을 담당합니다. Kafka로부터 주문 이벤트를 수신하여 메모리 내 오더북(OrderBook)에서 매칭을 수행하고, 체결 및 상태 변경 결과를 다시 Kafka로 발행합니다.

## 핵심 기술 및 패턴
- **Rust (2024 edition)**: 고성능 및 메모리 안전성 보장.
- **In-Memory OrderBook**: 매칭 성능 극대화를 위해 모든 활성 주문을 메모리(`BTreeMap`, `HashMap`)에서 관리.
- **Thread-safe Concurrency**: `crossbeam` 채널을 사용하여 수신(Consumer), 매칭(Engine), 송신(Producer) 로직을 독립된 스레드에서 병렬 처리.
- **Lock-free/Fine-grained Logic**: 현재 단일 워커 스레드 구조로 설계되어 순차적 매칭 보장 및 경쟁 상태 방지.

## 매칭 로직 (Matching Logic)
- **지원 주문 유형**:
  - `Limit`: 지정가 주문 (GTC, IOC, FOK 지원)
  - `Market`: 시장가 주문 (Buy/Sell, FOK 속성)
- **우선순위**: 가격(Price) -> 시간(Time) 순으로 체결 우선순위 부여.
- **매칭 루프**:
  1. Taker 주문 수신.
  2. 반대편 호가(Maker) 존재 여부 및 가격 조건 확인.
  3. 체결 가능 시 `Trade` 생성 및 호가창 업데이트.
  4. 잔량 존재 시 주문 유형에 따라 호가 등록 또는 취소 처리.

## 주요 데이터 구조
- `OrderBook`: 가격별 주문 큐(`VecDeque`)를 관리하는 `BTreeMap`과 주문 ID 기반 인덱스(`HashMap`).
- `Matcher`: 실제 매칭 알고리즘 구현체.
- `EngineManager`: 심볼별 매칭 워커 관리 및 명령 라우팅.

## 입출력 이벤트 (Kafka Topics)
- **Input**:
  - `place-order`: 주문 생성 요청.
  - `cancel-order`: 주문 취소 요청.
- **Output**:
  - `order-accepted`: 엔진 접수 완료.
  - `order-matched`: 주문 체결 발생 (Trade 목록 포함).
  - `order-canceled`: 주문 취소 완료 (IOC/FOK 만료 등).
  - `order-rejected`: 엔진 차원에서의 주문 거절.

## 성능 최적화 전략
- **Decimal 연산**: `rust_decimal`을 사용하여 금융 계산의 정확성 보장.
- **Zero-copy**: 가능한 경우 참조를 사용하여 데이터 복사 최소화.
- **Asynchronous I/O**: `rdkafka`를 통한 비동기 메시지 처리.
