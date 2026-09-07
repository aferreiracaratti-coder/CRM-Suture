package com.suture.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import com.suture.crm.syna.SynaEmbeddedProperties;
import com.suture.crm.auth.SutureAuthProperties;

@SpringBootApplication
@EnableConfigurationProperties({SynaEmbeddedProperties.class, SutureAuthProperties.class})
public class SutureBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SutureBackendApplication.class, args);
	}

	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

}
