package com.system.batch.killbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.system.batch.killbatch.section3_2.five"})
@SpringBootApplication
public class KillBatchApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(KillBatchApplication.class, args)));
	}

}
