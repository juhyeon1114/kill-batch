package com.system.batch.killbatch.section2_2;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * JobParameter로 받기 위해서는 @StepScope을 사용해야한다.
 */
@Getter
@StepScope
@Component
public class SystemInfiltrationParameters {
	@Value("#{jobParameters[missionName]}")
	private String missionName;
	private int securityLevel;
	private final String operationCommander;

	public SystemInfiltrationParameters(@Value("#{jobParameters[operationCommander]}") String operationCommander) {
		this.operationCommander = operationCommander;
	}

	@Value("#{jobParameters[securityLevel]}")
	public void setSecurityLevel(int securityLevel) {
		this.securityLevel = securityLevel;
	}

}
