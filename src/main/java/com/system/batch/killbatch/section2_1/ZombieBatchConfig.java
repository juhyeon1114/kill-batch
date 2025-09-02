package com.system.batch.killbatch.section2_1;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class ZombieBatchConfig {

	private final JobRepository jobRepository;
	private final ZombieProcessCleanupTasklet zombieProcessCleanupTasklet;

	/**
	 * - ResourcelessTransactionManager: no-op(아무것도 하지 않는) 방식으로 동작하는 PlatformTransactionManager 구현체로, 이를 사용하면 불필요한 DB 트랜잭션 처리를 생략할 수 있다.
	 */
	@Bean
	public Step zombieCleanupStep() {
		return new StepBuilder("zombieCleanupStep", jobRepository)
			.tasklet(zombieProcessCleanupTasklet, new ResourcelessTransactionManager())
			.build();
	}

	@Bean
	public Job zombieCleanupJob() {
		return new JobBuilder("zombieCleanupJob", jobRepository)
			.start(zombieCleanupStep())
			.build();
	}
}
