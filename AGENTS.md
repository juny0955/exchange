# Agent Instructions

## Context
본 프로젝트는 고성능 가상자산 거래소 플랫폼입니다. 모든 작업 시 다음 문서를 최우선으로 참조하십시오.

## Reference
- **Project Overview**: [project/project.md](docs/project/project.md) (전체 아키텍처 및 기술 스택)
- **Development Conventions**:
    - [Git Commit](docs/conventions/commit.md)
    - [Issue Template](docs/conventions/issue.md)
    - [PR Template](docs/conventions/pr.md)
- **Service Details**:
    - [Order Service](docs/project/order-service.md)
    - [Matching Engine](docs/project/matching-engine.md)
    - [Account Service](docs/project/account-service.md)

## Guidelines
- 문맥 파악이 필요할 경우 위 문서들을 먼저 읽으십시오.
- 코드 변경 시 각 서비스 문서에 정의된 아키텍처 패턴(헥사고날 등)과 기술 스택을 준수하십시오.
- 새로운 핵심 로직이나 테이블이 추가될 경우 관련 문서를 업데이트하십시오.
