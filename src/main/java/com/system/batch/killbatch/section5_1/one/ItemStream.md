## ItemStream

ItemStream은 Spring Batch의 핵심 인터페이스로 Spring Batch의 대부분의 ItemReader와 일부 ItemWriter 구현체에서 이 ItemStream이라는 인터페이스를 공통적으로 구현하고 있다.

## ItemStream의 역할

- 자원 초기화 및 해제
- 메타데이터 관리 및 상태 추적(스텝의 실행 정보를 저장 및 복구)

### 자원 초기화 및 해제

- ItemStream.open(): 자원 초기화
- ItemStream.close(): 자원 해제

### 자워 초기화 및 해제의 예시

- FlatFileItemReader: BufferReader 생성/해제
- JdbcCursorItemReader: DB 커넥션, 커서 생성/해제
- FlatFileItemWriter: BufferWriter 생성/해제
