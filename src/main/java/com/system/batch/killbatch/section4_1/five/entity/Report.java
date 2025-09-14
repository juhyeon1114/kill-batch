package com.system.batch.killbatch.section4_1.five.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "reports")
@Getter
public class Report {

	@Id
	private Long id;
	private String reportType;
	private int reporterLevel;
	private String evidenceData;
	private LocalDateTime reportedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

}
