package com.system.batch.killbatch.section3_3.two;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

/**
 * JsonItemReader, JsonFileItemWriter를 활용하여 JSON 읽고 쓰기
 */
@RequiredArgsConstructor
@Configuration
public class SystemDeathJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job systemDeathJob(Step systemDeathStep) {
		return new JobBuilder("systemDeathJob", jobRepository)
			.start(systemDeathStep)
			.build();
	}

	@Bean
	public Step systemDeathStep(
		JsonItemReader<SystemDeath> systemDeathReader,
		JsonFileItemWriter<DeathNote> deathNoteJsonWriter
	) {
		return new StepBuilder("systemDeathStep", jobRepository)
			.<SystemDeath, DeathNote>chunk(10, transactionManager)
			.reader(systemDeathReader)
			.writer(deathNoteJsonWriter)
			.build();
	}

	@Bean
	@StepScope
	public JsonItemReader<SystemDeath> systemDeathReader(
		@Value("#{jobParameters['inputFile']}") String inputFile
	) {
		return new JsonItemReaderBuilder<SystemDeath>()
			.name("systemDeathReader")
			.jsonObjectReader(new JacksonJsonObjectReader<>(SystemDeath.class))
			.resource(new FileSystemResource(inputFile))
			.build();
	}

	@Bean
	@StepScope
	public JsonFileItemWriter<DeathNote> deathNoteJsonWriter(
		@Value("#{jobParameters['outputDir']}") String outputDir
	) {
		return new JsonFileItemWriterBuilder<DeathNote>()
			.jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
			.resource(new FileSystemResource(outputDir + "/death_notes.json"))
			.name("logEntryJsonWriter")
			.build();
	}

	public record DeathNote(
		String victimId,
		String victimName,
		String executionDate,
		String causeOfDeath
	) {
	}

	public record SystemDeath(String command, int cpu, String status) {
	}
}
