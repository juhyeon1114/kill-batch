package com.system.batch.killbatch.section3_1.one;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SystemFailureJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job systemFailureJob(Step systemFailureStep) {
		return new JobBuilder("systemFailureJob", jobRepository)
			.start(systemFailureStep)
			.build();
	}

	@Bean
	public Step systemFailureStep(
		FlatFileItemReader<SystemFailure> systemFailureItemReader,
		SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
	) {
		return new StepBuilder("systemFailureStep", jobRepository)
			.<SystemFailure, SystemFailure>chunk(10, transactionManager)
			.reader(systemFailureItemReader)
			.writer(systemFailureStdoutItemWriter)
			.build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<SystemFailure> systemFailureItemReader(
		@Value("#{jobParameters['inputFile']}") String inputFile
	) {
		return new FlatFileItemReaderBuilder<SystemFailure>()
			.name("systemFailureItemReader") // ItemReader의 식별자 지정
			.resource(new FileSystemResource(inputFile)) // 대상 파일 설정
			.delimited() // 파일 형식 지정
			.delimiter(",") // 구분자 지정
			.names( // 프로퍼티 맵핑
				"errorId",
				"errorDateTime",
				"severity",
				"processId",
				"errorMessage"
			)
			.targetType(SystemFailure.class) // 맵핑 대상 글래스 지정
			.linesToSkip(1) // '헤더'에 해당하는 라인 지정
			.build();
	}

	@Bean
	public SystemFailureStdoutItemWriter systemFailureStdoutItemWriter() {
		return new SystemFailureStdoutItemWriter();
	}

	public static class SystemFailureStdoutItemWriter implements ItemWriter<SystemFailure> {
		@Override
		public void write(Chunk<? extends SystemFailure> chunk) throws Exception {
			for (SystemFailure failure : chunk) {
				log.info("Processing system failure: {}", failure);
			}
		}
	}

	@Data
	public static class SystemFailure {
		private String errorId;
		private String errorDateTime;
		private String severity;
		private Integer processId;
		private String errorMessage;
	}
}
