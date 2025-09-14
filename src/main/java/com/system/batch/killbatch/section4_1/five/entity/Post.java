package com.system.batch.killbatch.section4_1.five.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@NamedQuery(
	name = "Post.findByReportsReportedAtBetween",
	query = "SELECT p FROM Post p JOIN FETCH p.reports r WHERE r.reportedAt >= :startDateTime AND r.reportedAt < :endDateTime"
)
@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {

	@Id
	private Long id;
	private String title;
	private String content;
	private String writer;
	private LocalDateTime blockedAt;  // 차단 일시 필드 추가

	@OneToMany(mappedBy = "post", fetch = FetchType.EAGER) // FetchType EAGER 변경
	@BatchSize(size = 5) // @BatchSize 적용
	private List<Report> reports = new ArrayList<>();

}
