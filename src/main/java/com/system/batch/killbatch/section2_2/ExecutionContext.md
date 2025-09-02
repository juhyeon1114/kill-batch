## ExecutionContext

Spring Batch는 JobExecution과 StepExecution을 사용해 시작 시간, 종료 시간, 실행 상태 같은 메타데이터를 관리한다. 하지만 이런 기본적인 실행 정보만으로는 시스템을 완벽하게 제어하기 부족할 때가 있다.
비즈니스 로직 처리 중에 발생하는 커스텀 데이터를 관리할 방법이 필요한데, 이때 사용하는 것이 바로 ExecutionContext라는 데이터 컨테이너다.


ExecutionContext의 데이터 역시 JobParameters와 마찬가지로 @Value를 통해 주입받을 수 있다. 다음 예제를 보자.

```java
@Bean
@JobScope  
public Tasklet systemDestructionTasklet(
  @Value("#{jobExecutionContext['previousSystemState']}") String prevState
) {
  // JobExecution의 ExecutionContext에서 이전 시스템 상태를 주입받는다
}

@Bean
@StepScope
public Tasklet infiltrationTasklet(
  @Value("#{stepExecutionContext['targetSystemStatus']}") String targetStatus
) {
  // StepExecution의 ExecutionContext에서 타겟 시스템 상태를 주입받는다
}
```

Step의 ExecutionContext에 저장된 데이터는 `@Value("#{jobExecutionContext['key']}")`로 접근할 수 없다.
즉, Step 수준의 데이터를 Job 수준에서 가져올 수 없다.

한 Step의 ExecutionContext는 다른 Step에서 접근할 수 없다.
예를 들어 StepA의 ExecutionContext에 저장된 데이터를 StepB에서 `@Value("#{stepExecutionContext['key']}")`로 가져올 수 없다.

Spring Batch가 이렇게 Step의 ExecutionContext 접근을 엄격하게 제한하는 이유는 Step 간의 데이터 독립성을 완벽하게 보장하기 위해서다. 하지만 이전 Step의 처리 결과를 다음 Step에서 활용하고 싶을 수도 있다.
