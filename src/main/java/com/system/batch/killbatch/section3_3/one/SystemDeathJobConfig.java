package com.system.batch.killbatch.section3_3.one;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.JsonRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * 커스텀 LineMapper를 활용한 json 문자열 읽기
 */
@RequiredArgsConstructor
@Configuration
public class SystemDeathJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Bean
	public Job systemDeathJob(Step systemDeathStep) {
		return new JobBuilder("systemDeathJob", jobRepository)
			.start(systemDeathStep)
			.build();
	}

	@Bean
	public Step systemDeathStep(
		FlatFileItemReader<SystemDeath> systemDeathReader
	) {
		return new StepBuilder("systemDeathStep", jobRepository)
			.<SystemDeath, SystemDeath>chunk(10, transactionManager)
			.reader(systemDeathReader)
			.writer(items -> items.forEach(System.out::println))
			.build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<SystemDeath> systemDeathReader(
		@Value("#{jobParameters['inputFile']}") String inputFile
	) {
		return new FlatFileItemReaderBuilder<SystemDeath>()
			.name("systemDeathReader")
			.resource(new FileSystemResource(inputFile))
			.lineMapper((line, lineNumber) -> objectMapper.readValue(line, SystemDeath.class))
			.recordSeparatorPolicy(new JsonRecordSeparatorPolicy())  // Json 객체의 경계를 감지하도록 정책 주입
			.build();
	}

	public record SystemDeath(String command, int cpu, String status) {
	}
}
