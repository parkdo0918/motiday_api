package com.example.motiday_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MotidayApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotidayApiApplication.class, args);
    }

}
