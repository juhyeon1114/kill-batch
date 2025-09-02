package com.system.batch.killbatch.section1;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class SystemTerminationConfig {

	private AtomicInteger processesKilled = new AtomicInteger(0);
	private final int TERMINATION_TARGET = 5;

	/**
	 * - JobRepository: Job의 실행 이력, 상태, 결과 등의 메타데이터가 관리됨.
	 * - PlatformTransactionManager: tasklet을 하나의 DB 트랜잭션으로 관리하기 위함.
	 */
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	/**
	 * - Job 구성
	 */
	@Bean
	public Job systemTerminationSimulationJob() {
		return new JobBuilder("systemTerminationSimulationJob", jobRepository)// Job의 이름
			.start(enterWorldStep()) // 첫번째 Step
			.next(meetNPCStep())
			.next(defeatProcessStep())
			.next(completeQuestStep())
			.build();
	}

	/**
	 * - Step의 실제 동작은 tasklet에 정의된다.
	 */
	@Bean
	public Step enterWorldStep() {
		return new StepBuilder("enterWorldStep", jobRepository)
			.tasklet(
				(contribution, chunkContext) -> {
					log.info("System Termination 시뮬레이션 세계에 접속했습니다!");
					return RepeatStatus.FINISHED;
				}, transactionManager
			)
			.build();
	}

	@Bean
	public Step meetNPCStep() {
		return new StepBuilder("meetNPCStep", jobRepository)
			.tasklet(
				(contribution, chunkContext) -> {
					log.info("시스템 관리자 NPC를 만났습니다.");
					log.info("첫 번째 미션: 좀비 프로세스 " + TERMINATION_TARGET + "개 처형하기");
					return RepeatStatus.FINISHED;
				}, transactionManager
			)
			.build();
	}

	@Bean
	public Step defeatProcessStep() {
		return new StepBuilder("defeatProcessStep", jobRepository)
			.tasklet(
				(contribution, chunkContext) -> {
					int terminated = processesKilled.incrementAndGet();
					log.info("좀비 프로세스 처형 완료! (현재 {}/" + TERMINATION_TARGET + ")", terminated);
					if (terminated < TERMINATION_TARGET) {
						return RepeatStatus.CONTINUABLE;
					} else {
						return RepeatStatus.FINISHED;
					}
				}, transactionManager
			)
			.build();
	}

	@Bean
	public Step completeQuestStep() {
		return new StepBuilder("completeQuestStep", jobRepository)
			.tasklet(
				(contribution, chunkContext) -> {
					log.info("미션 완료! 좀비 프로세스 " + TERMINATION_TARGET + "개 처형 성공!");
					log.info("보상: kill -9 권한 획득, 시스템 제어 레벨 1 달성");
					return RepeatStatus.FINISHED;
				}, transactionManager
			)
			.build();
	}
}
