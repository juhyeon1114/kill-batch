package com.system.batch.killbatch.section4_1.two;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JdbcCursorItemReader
 * - 스텝이 끝난 후, 결과가 DB에 커밋되더라도 DB 연결을 해제하지 않고 스트리밍 방식으로 데이터를 불러오고 처리하는 방식의 Reader
 *
 * JdbcCursorItemReader의 동작 방식
 * - JDBC 드라이버 내부 버퍼(ResultSet)을 먼저 확인 하고, 데이터가 없으면 DB에서 ResultSet으로 데이터를 불러옴.
 * - ResultSet에 저장된 데이터를 하나씩 처리
 * - ResultSet에 올려둘 데이터의 크기는 `fetchSize`로 최적화 가능
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class VictimRecordConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final DataSource dataSource;

	@Bean
	public Job processVictimJob() {
		return new JobBuilder("victimRecordJob", jobRepository)
			.start(processVictimStep())
			.build();
	}

	@Bean
	public Step processVictimStep() {
		return new StepBuilder("victimRecordStep", jobRepository)
			.<Victim, Victim>chunk(5, transactionManager)
			.reader(terminatedVictimReader())
			.writer(victimWriter())
			.build();
	}

	@Bean
	public JdbcCursorItemReader<Victim> terminatedVictimReader() {
		return new JdbcCursorItemReaderBuilder<Victim>()
			.name("terminatedVictimReader")
			.dataSource(dataSource)
			.sql("SELECT * FROM victims WHERE status = ? AND terminated_at <= ?")
			.queryArguments(List.of("TERMINATED", LocalDateTime.now()))
			// .beanRowMapper(Victim.class) // 쿼리 결과와 클래스를 맵핑
			// .rowMapper(new DataClassRowMapper<>(Victim.class)) // 쿼리 결과와 Record를 맵핑
			.rowMapper((rs, rowNum) -> { // 람다로 직접 맵핑 규칙 정의
				Victim victim = new Victim();
				victim.setId(rs.getLong("id"));
				victim.setName(rs.getString("name"));
				victim.setProcessId(rs.getString("process_id"));
				victim.setTerminatedAt(rs.getTimestamp("terminated_at").toLocalDateTime());
				victim.setStatus(rs.getString("status"));
				return victim;
			})
			.build();
	}

	@Bean
	public ItemWriter<Victim> victimWriter() {
		return items -> {
			for (Victim victim : items) {
				log.info("{}", victim);
			}
		};
	}

	@NoArgsConstructor
	@Data
	public static class Victim {
		private Long id;
		private String name;
		private String processId;
		private LocalDateTime terminatedAt;
		private String status;
	}

}
