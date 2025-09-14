package com.system.batch.killbatch.section4_1.five;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import com.system.batch.killbatch.section4_1.five.entity.Post;
import com.system.batch.killbatch.section4_1.five.entity.Report;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PostBlockBatchConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final EntityManagerFactory entityManagerFactory;

	@Bean
	public Job postBlockBatchJob(Step postBlockStep) {
		return new JobBuilder("postBlockBatchJob", jobRepository)
			.start(postBlockStep)
			.build();
	}

	@Bean
	public Step postBlockStep(
		// JpaCursorItemReader<Post> postBlockReader,
		JpaPagingItemReader<Post> postBlockReader,
		PostBlockProcessor postBlockProcessor
		// ItemWriter<BlockedPost> postBlockWriter
	) {
		return new StepBuilder("postBlockStep", jobRepository)
			.<Post, Post>chunk(5, transactionManager)
			.reader(postBlockReader)
			.processor(postBlockProcessor)
			.writer(postBlockWriter())
			.build();
	}

	/**
	 * queryString()을 활용한 가장 간단한 형태로 조회 쿼리를 선언하는 Reader
	 */
	// @Bean
	// @StepScope
	// public JpaCursorItemReader<Post> postBlockReader(
	// 	@Value("#{jobParameters['startDateTime']}") LocalDateTime startDateTime,
	// 	@Value("#{jobParameters['endDateTime']}") LocalDateTime endDateTime
	// ) {
	// 	return new JpaCursorItemReaderBuilder<Post>()
	// 		.name("postBlockReader")
	// 		.entityManagerFactory(entityManagerFactory)
	// 		.queryString("""
	// 			SELECT p FROM Post p JOIN FETCH p.reports r
	// 			WHERE r.reportedAt >= :startDateTime AND r.reportedAt < :endDateTime
	// 			""")
	// 		.parameterValues(Map.of(
	// 			"startDateTime", startDateTime,
	// 			"endDateTime", endDateTime
	// 		))
	// 		.build();
	// }

	/**
	 * queryProvider()을 활용하여 동적이며 좀 더 복잡한 형태의 쿼리를 만들 수 있음.
	 */
	// @Bean
	// public JpaCursorItemReader<Post> postBlockReader() {
	// 	LocalDateTime now = LocalDateTime.now();
	// 	LocalDateTime startDateTime = now.minusDays(14);
	// 	LocalDateTime endDateTime = now.plusDays(1);
	//
	// 	return new JpaCursorItemReaderBuilder<Post>()
	// 		.name("postBlockReader")
	// 		.entityManagerFactory(entityManagerFactory)
	// 		.queryProvider(createQueryProvider())
	// 		.parameterValues(Map.of(
	// 			"startDateTime", startDateTime,
	// 			"endDateTime", endDateTime
	// 		))
	// 		.build();
	// }
	// private JpaNamedQueryProvider<Post> createQueryProvider() {
	// 	JpaNamedQueryProvider<Post> queryProvider = new JpaNamedQueryProvider<>();
	// 	queryProvider.setEntityClass(Post.class);
	// 	queryProvider.setNamedQuery("Post.findByReportsReportedAtBetween");
	// 	return queryProvider;
	// }
	@Bean
	@StepScope
	public JpaPagingItemReader<Post> postBlockReader(
		@Value("#{jobParameters['startDateTime']}") LocalDateTime startDateTime,
		@Value("#{jobParameters['endDateTime']}") LocalDateTime endDateTime
	) {
		return new JpaPagingItemReaderBuilder<Post>()
			.name("postBlockReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("""
				SELECT DISTINCT p FROM Post p 
				JOIN p.reports r
				WHERE r.reportedAt >= :startDateTime AND r.reportedAt < :endDateTime
				ORDER BY p.id ASC
				""")
			.parameterValues(Map.of(
				"startDateTime", startDateTime,
				"endDateTime", endDateTime
			))
			.pageSize(5) // 한번의 쿼리로 불러올 데이터의 수. LIMIT의 값으로 사용됨.
			.transacted(false) // false: 엔티티를 영속성 컨텍스트에서 분리하여, Lazy loading이 불가능하도록 하며, Eager 로딩이 가능하도록 한다.
			.build();
	}

	// @Bean
	// public ItemWriter<BlockedPost> postBlockWriter() {
	// 	return items -> {
	// 		items.forEach(blockedPost -> {
	// 			log.info(
	// 				":해골: TERMINATED: [ID:{}] '{}' by {} | 신고:{}건 | 점수:{} | kill -9 at {}",
	// 				blockedPost.getPostId(),
	// 				blockedPost.getTitle(),
	// 				blockedPost.getWriter(),
	// 				blockedPost.getReportCount(),
	// 				String.format("%.2f", blockedPost.getBlockScore()),
	// 				blockedPost.getBlockedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
	// 			);
	// 		});
	// 	};
	// }

	@Bean
	public JpaItemWriter<Post> postBlockWriter() {
		return new JpaItemWriterBuilder<Post>()
			.entityManagerFactory(entityManagerFactory)
			// .usePersist(true) // true: persist() 사용
			.usePersist(false) // false: merge() 사용
			.build();
	}

	@Component
	public static class PostBlockProcessor implements ItemProcessor<Post, Post> {
		@Override
		public Post process(Post post) {
			// 각 신고의 신뢰도를 기반으로 차단 점수 계산
			double blockScore = calculateBlockScore(post.getReports());

			// 차단 점수가 기준치를 넘으면 처형 결정
			if (blockScore >= 7.0) {
				post.setBlockedAt(LocalDateTime.now());
				return post;
			}

			return null;  // 무죄 방면
		}

		private double calculateBlockScore(List<Report> reports) {
			// 각 신고들의 정보를 시그니처에 포함시켜 마치 사용하는 것처럼 보이지만...
			for (Report report : reports) {
				analyzeReportType(report.getReportType());            // 신고 유형 분석
				checkReporterTrust(report.getReporterLevel());        // 신고자 신뢰도 확인
				validateEvidence(report.getEvidenceData());           // 증거 데이터 검증
				calculateTimeValidity(report.getReportedAt());        // 시간 가중치 계산
			}
			// 실제로는 그냥 랜덤 값을 반환
			return Math.random() * 10;  // 0~10 사이의 랜덤 값
		}

		// 아래는 실제로는 아무것도 하지 않는 메서드들
		private void analyzeReportType(String reportType) {
			// 신고 유형 분석하는 척
		}

		private void checkReporterTrust(int reporterLevel) {
			// 신고자 신뢰도 확인하는 척
		}

		private void validateEvidence(String evidenceData) {
			// 증거 검증하는 척
		}

		private void calculateTimeValidity(LocalDateTime reportedAt) {
			// 시간 가중치 계산하는 척
		}
	}
}
