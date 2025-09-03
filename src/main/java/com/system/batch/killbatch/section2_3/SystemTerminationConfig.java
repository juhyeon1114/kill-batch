package com.system.batch.killbatch.section2_3;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * - ExecutionContextPromotionListener는 Step 수준 ExecutionContext의 데이터를 Job 수준 ExecutionContext로 등록시켜주는 StepExecutionListener의 구현체다.
 * - Spring Batch에서는 Step 수준의 ExecutionContext 데이터를 Job 수준의 ExecutionContext로 옮기는 과정을 승격(Promote)이라 부른다.
 * - 이 리스너는 StepExecutionListener의 afterStep() 메서드를 오버라이드하여 승격 작업을 수행한다.
 */
@Slf4j
@Configuration
public class SystemTerminationConfig {
	@Bean
	public Job systemTerminationJob(JobRepository jobRepository, Step scanningStep, Step eliminationStep) {
		return new JobBuilder("systemTerminationJob", jobRepository)
			.start(scanningStep)
			.next(eliminationStep)
			.build();
	}

	@Bean
	public Step scanningStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager
	) {
		return new StepBuilder("scanningStep", jobRepository)
			.tasklet(
				(contribution, chunkContext) -> {
					String target = "판교 서버실";
					ExecutionContext stepContext = contribution.getStepExecution().getExecutionContext();
					stepContext.put("targetSystem", target);
					log.info("타겟 스캔 완료: {}", target);
					return RepeatStatus.FINISHED;
				}, transactionManager
			)
			.listener(promotionListener()) // promotionListener 등록
			.build();
	}

	@Bean
	public Step eliminationStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		Tasklet eliminationTasklet
	) {
		return new StepBuilder("eliminationStep", jobRepository)
			.tasklet(eliminationTasklet, transactionManager)
			.build();
	}

	@Bean
	@StepScope
	public Tasklet eliminationTasklet(
		@Value("#{jobExecutionContext['targetSystem']}") String target // 승격된 targetSystem에 접근
	) {
		return (contribution, chunkContext) -> {
			log.info("시스템 제거 작업 실행: {}", target);
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public ExecutionContextPromotionListener promotionListener() {
		ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
		listener.setKeys(new String[] {"targetSystem"}); // targetSystem 키를 승격 대상으로 지정
		return listener;
	}
}
