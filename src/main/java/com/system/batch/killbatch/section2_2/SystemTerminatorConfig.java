package com.system.batch.killbatch.section2_2;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * JobParameters
 * - 입력값을 동적으로 변경할 수 있다.
 * - Spring batch는 JobParameters의 모든 값을 메타데이터 저장소에 기록하며, 이를 통해 Job 인스턴스 식별 및 재시작, Job 실행 이력 추적을 할 수 있다.

 * JobParameters의 표기법
 * - parameterName=parameterValue,parameterType,identificationFlag
 * - parameterName: 파라미터의 Key 값
 * - parameterValue: 파라미터의 실제 값
 * - parameterType: java.lang.String, java.lang.Integer와 같은 파라미터 값의 타입 (기본값은 java.lang.String)
 * - identificationFlag: 파라미터가 JobInstance  식별에 사용될 파라미터인지에 대한 여부
 */
@Slf4j
// @Configuration
public class SystemTerminatorConfig {

	@Bean
	public Job processTerminatorJob(JobRepository jobRepository, Step terminationStep) {
		return new JobBuilder("processTerminatorJob", jobRepository)
			.start(terminationStep)
			.build();
	}

	@Bean
	public Step terminationStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		Tasklet terminatorTasklet
	) {
		return new StepBuilder("terminationStep", jobRepository)
			.tasklet(terminatorTasklet, transactionManager)
			.build();
	}

	@Bean
	@StepScope
	public Tasklet terminatorTasklet(
		@Value("#{jobParameters['terminatorId']}") String terminatorId,
		@Value("#{jobParameters['targetCount']}") Integer targetCount
	) {
		return (contribution, chunkContext) -> {
			log.info("시스템 종결자 정보:");
			log.info("ID: {}", terminatorId);
			log.info("제거 대상 수: {}", targetCount);
			log.info("⚡ SYSTEM TERMINATOR {} 작전을 개시합니다.", terminatorId);
			log.info("☠️ {}개의 프로세스를 종료합니다.", targetCount);

			for (int i = 1; i <= targetCount; i++) {
				log.info("💀 프로세스 {} 종료 완료!", i);
			}

			log.info("🎯 임무 완료: 모든 대상 프로세스가 종료되었습니다.");
			return RepeatStatus.FINISHED;
		};
	}
}
