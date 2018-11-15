package com.jvmfy.digitalsignature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DigitalSignatureApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalSignatureApplication.class, args);
    }
}
