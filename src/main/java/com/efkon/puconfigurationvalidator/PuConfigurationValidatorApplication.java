package com.efkon.puconfigurationvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.efkon.puconfigurationvalidator.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({ FileStorageProperties.class })
public class PuConfigurationValidatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PuConfigurationValidatorApplication.class, args);
	}

}
