## ItemProcessor의 역할

### 1) null 반환을 통한 데이터 필터링

ItemProcessor의 process() 메서드가 null을 반환하면 해당 item은 ItemWriter로 전달되는 Chunk에서 완전히 제외된다.
이것이 ItemProcessor를 통한 데이터 필터링이다.


### 2) 데이터 검증을 통한 실패 처리

```java
@Bean
public ItemProcessor<Command, Command> commandProcessor() {
    ValidatingItemProcessor<Command> processor = 
        new ValidatingItemProcessor<>(new CommandValidator());
    processor.setFilter(false);  // 기본값, ValidationException 발생시 그대로 예외 전파
    return processor;
}
```

유효하지 않은 데이터 하나만 발견되어도 즉시 예외를 던져 전체 배치 잡을 중단시키는 것이다.

### 3) 데이터 변환

```java
public interface ItemProcessor<I, O> {
    @Nullable
    O process(@NonNull I item) throws Exception;
}
```

읽어온 데이터를 우리가 원하는 형태로 변환한다.

### 4) 데이터 보강 (Data Enrichment)

외부 시스템이나 데이터베이스에서 추가 정보를 가져와 기존 데이터를 보강할 수 있다.
원본 데이터에 외부 소스로부터 얻은 정보를 추가하여 더욱 풍부하고 의미 있는 데이터로 만들 수 있다.
