package com.dev.ci_cd_service;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class CiCdServiceApplication {
    @PostConstruct
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

	public static void main(String[] args) {
		SpringApplication.run(CiCdServiceApplication.class, args);
	}

}
