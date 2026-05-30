package com.otp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    JdbcTemplateAutoConfiguration.class
})
public class OtpApplication {
    public static void main(String[] args) {
        SpringApplication.run(OtpApplication.class, args);
        System.out.println("========================================");
        System.out.println("OTP Service Started Successfully!");
        System.out.println("API available at: http://localhost:8080");
        System.out.println("========================================");
    }
}