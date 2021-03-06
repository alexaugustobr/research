package com.github.paulosalonso.research.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.github.paulosalonso.research")
@ComponentScan(basePackages = "com.github.paulosalonso.research")
@EnableJpaRepositories(basePackages = "com.github.paulosalonso.research")
@EntityScan(basePackages = "com.github.paulosalonso.research")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResearchApplication.class, args);
	}

}
