## JobScope

```java
@Bean
@JobScope
public Step systemDestructionStep(
        @Value("#{jobParameters['destructionPower']}") Long destructionPower) {
    return new StepBuilder("systemDestructionStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            log.info("시스템 파괴 프로세스가 시작되었습니다. 파괴력: {}", destructionPower);
            return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
}
```

- JobScope이 붙은 Bean은 Job이 실행될 때, Bean이 생성되고, Job이 종료되면 함께 제거된다.
- REST API로 Job 시작 요청을 받아 실행하는 애플리케이션을 떠올려보자. 웹 요청이 매번 다른 파라미터를 전달할 수 있다. @JobScope는 이런 동적 파라미터를 Step 빈 생성 시에 주입받을 수 있게 해준다. 만약 @JobScope가 없다면 Step 빈은 애플리케이션 구동 시점에 생성되므로, 실행 시점에 전달되는 파라미터를 받을 수 없을 것이다.

<br>
<br>

## StepScope

```java
@Bean
public Step infiltrationStep(
    JobRepository jobRepository,
    PlatformTransactionManager transactionManager,
    Tasklet systemInfiltrationTasklet
) {
    return new StepBuilder("infiltrationStep", jobRepository)
        .tasklet(systemInfiltrationTasklet, transactionManager)
        .build();
}

@Bean 
@StepScope 
public Tasklet systemInfiltrationTasklet(
    @Value("#{jobParameters['infiltrationTargets']}") String infiltrationTargets
) {
    return (contribution, chunkContext) -> {
        String[] targets = infiltrationTargets.split(",");
        log.info("시스템 침투 시작");
        log.info("주 타겟: {}", targets[0]);
        log.info("보조 타겟: {}", targets[1]);
        log.info("침투 완료");
        return RepeatStatus.FINISHED;
    };
}
```

- StepScope 붙은 Bean은 Step이 실행될 때, Bean이 생성되고, Step이 종료되면 함께 제거된다.
- Tasklet은 Step마다 독립적으로 생성되어야 하므로, StepScope을 붙여준다.

<br>
<br>

## JobScope, StepScope 사용 시 주의사항

### 1) 프록시 대상의 타입이 클래스인 경우 반드시 상속 가능한 클래스여야 한다.

```java
@Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobScope {}
```

### 2) Step 빈에는 @StepScope와 @JobScope와를 사용하지 말라.

```java
@Bean
@StepScope
public Step systemDestructionStep(
    SystemInfiltrationTasklet tasklet
) {
    return new StepBuilder("systemDestructionStep", jobRepository)
        .tasklet(tasklet, transactionManager)
        .build();
}
```
