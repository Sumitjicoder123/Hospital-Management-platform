package com.hospital.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HospitalPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospitalPlatformApplication.class, args);
    }
}
