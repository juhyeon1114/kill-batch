package com.system.batch.killbatch.section2_1;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Tasklet 지향 처리
 * - 단순한 시스템 작업이나 유틸성 작업을 처리할 때 주로 사용하는 방식
 */
@Slf4j
@Component
public class ZombieProcessCleanupTasklet implements Tasklet {

	private final int processesToKill = 10;
	private int killedProcesses = 0;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		killedProcesses++;
		log.info("☠️  프로세스 강제 종료... ({}/{})", killedProcesses, processesToKill);

		if (killedProcesses >= processesToKill) {
			log.info("💀 시스템 안정화 완료. 모든 좀비 프로세스 제거.");
			return RepeatStatus.FINISHED;  // 모든 프로세스 종료 후 작업 완료
		}

		return RepeatStatus.CONTINUABLE;  // 아직 더 종료할 프로세스가 남아있음
	}

}
