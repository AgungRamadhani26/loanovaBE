package com.example.loanova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
// Loan Oriented Analytics & Verification App
public class LoanovaApplication {

  public static void main(String[] args) {
    SpringApplication.run(LoanovaApplication.class, args);
  }
}
