## 청크 지향 처리

- Chunk: 데이터를 작은 덩어리로 나누어서 처리하는 방식
  - Ex) 100만 건을 100개의 청크로 나눈다면, 하나의 청크는 1만 건의 데이터를 갖게 된다.

### 청크를 쓰는 이유

- 메모리를 안정적으로 사용하기 위함
- 가벼운 트랜잭션 → 작은 실패

### 청크 지향 처리의 패턴

- ItemReader, ItemProcessor, ItemWriter: 읽기 → 가공 → 쓰기
  - 책임 분리
  - 재사용성 향상
  - 높은 유연성
  - 대용량 처리의 표준

### ItemReader

```java
public interface ItemReader<T> { 
	T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
}
```

- 하나씩: `read()`는 아이템을 하나씩 반환한다.
- 다양한 구현체 제공: Spring batch는 파일, DB, 메세지 큐 등 다양한 데이터 소스에 대한 표준 구현체를 제공한다.

### ItemProcessor

```java
public interface ItemProcessor<I, O> {
    O process(I item) throws Exception;
}
```

- 데이터 가공: 입력 데이터(I)를 원하는 형태(O)로 변환한다. 예를 들어, 읽어온 원본 데이터를 비즈니스 로직에 맞게 가공할 수 있다.
- 필터링: `null`을 반환하면 해당 데이터는 처리 흐름에서 제외된다. 다시 말해 ItemWriter로 전달되지 않는다. 유효하지 않은 데이터나 처리할 필요가 없는 데이터를 걸러낼 때 사용한다.
- 데이터 검증: 입력 데이터의 유효성을 검사한다. 필터링과 달리 조건에 맞지 않는 데이터를 만나면 예외를 발생시킨다. 예를 들어, 필수 필드 누락이나 잘못된 데이터 형식을 발견했을 때 예외를 던져 배치 잡을 중단시킨다.
- 필수 아님: ItemProcessor는 생략 가능하다.


### ItemWriter

```java
public interface ItemWriter<T> {
    void write(Chunk<? extends T> chunk) throws Exception;
}
```

- 한 덩어리씩 쓴다: ItemWriter는 데이터를 한 건씩 쓰지 않는다. Chunk 단위로 묶어서 한 번에 데이터를 쓴다.
- 다양한 구현체 제공: Spring Batch는 파일, 데이터베이스, 외부 시스템 전송 등에 사용할 수 있는 다양한 구현체를 제공한다. 

<br>
<br>

## 예시

```java
@Bean
public Step processStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
   return new StepBuilder("processStep", jobRepository)
           .<CustomerDetail, CustomerSummary>chunk(10, transactionManager)  // 청크 지향 처리 활성화
           .reader(itemReader())       // 데이터 읽기 담당
           .processor(itemProcessor()) // 데이터 처리 담당
           .writer(itemWriter())      // 데이터 쓰기 담당
           .build();
}

@Bean
public Job customerProcessingJob(JobRepository jobRepository, Step processStep) {
   return new JobBuilder("customerProcessingJob", jobRepository)
           .start(processStep)  // processStep으로 Job 시작
           .build();
}
```


<br>
<br>

## 청크

### 트랜잭션

- 트랜잭션은 청크 단위로 묶이게 된다.

### 적절한 청크 사이즈란?

- 청크 사이즈가 크다 → 메모리 부하가 커지며, 트랜잭션으로 묶이는 데이터가 많아서, 실패 시 롤백되는 데이터의 수도 커진다.
- 청크 사이즈가 작다 → 롤백되는 데이터의 수가 작다. 읽기/쓰기 IO가 자주 발생하게 되어서, 처리 속도가 늘어나게 된다.
