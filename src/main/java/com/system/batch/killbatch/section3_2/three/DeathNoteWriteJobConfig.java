package com.system.batch.killbatch.section3_2.three;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DeathNoteWriteJobConfig {

	@Bean
	public Job deathNoteWriteJob(
		JobRepository jobRepository,
		Step deathNoteWriteStep
	) {
		return new JobBuilder("deathNoteWriteJob", jobRepository)
			.start(deathNoteWriteStep)
			.build();
	}

	@Bean
	public Step deathNoteWriteStep(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		ListItemReader<DeathNote> deathNoteListReader,
		FlatFileItemWriter<DeathNote> deathNoteWriter
	) {
		return new StepBuilder("deathNoteWriteStep", jobRepository)
			.<DeathNote, DeathNote>chunk(10, transactionManager)
			.reader(deathNoteListReader)
			.writer(deathNoteWriter)
			.build();
	}

	@Bean
	public ListItemReader<DeathNote> deathNoteListReader() {
		List<DeathNote> victims = List.of(
			new DeathNote(
				"KILL-001",
				"김배치",
				"2024-01-25",
				"CPU 과부하"
			),
			new DeathNote(
				"KILL-002",
				"사불링",
				"2024-01-26",
				"JVM 스택오버플로우"
			),
			new DeathNote(
				"KILL-003",
				"박탐묘",
				"2024-01-27",
				"힙 메모리 고갈"
			)
		);

		return new ListItemReader<>(victims);
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<DeathNote> deathNoteWriter(
		@Value("#{jobParameters['outputDir']}") String outputDir
	) {
		return new FlatFileItemWriterBuilder<DeathNote>()
			.name("deathNoteWriter")
			.resource(new FileSystemResource(outputDir + "/death_note_report.txt"))
			.formatted() // LineAggregator의 구현체로 FormatterLineAggregator를 지정. 객체의 각 필드를 지정된 포맷 문자열에 맞춰서 하나의 문자열로 변환.
			.format("처형 ID: %s | 처형일자: %s | 피해자: %s | 사인: %s")
			.sourceType(DeathNote.class)
			.names("victimId", "executionDate", "victimName", "causeOfDeath")
			// .shouldDeleteIfExists(true) // 기존 파일을 삭제할 것인지 여부
			// .append(true) // 기존 파일에 덧붙일 것인지 여부
			// .shouldDeleteIfEmpty(true) // 빈 결과 파일을 삭제할 것인지 여부
			.headerCallback(writer -> writer.write("================= 처형 기록부 ================="))
			.footerCallback(writer -> writer.write("================= 처형 완료 =================="))
			.build();
	}

	public record DeathNote(
		String victimId,
		String victimName,
		String executionDate,
		String causeOfDeath
	) {
	}

}
