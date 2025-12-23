package com.Suchorit.AttributeAuthority;

import jakarta.persistence.Entity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AttributeAuthorityApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttributeAuthorityApplication.class, args);
	}

}
