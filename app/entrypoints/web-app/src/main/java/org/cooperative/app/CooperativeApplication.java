package org.cooperative.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.cooperative"})
public class CooperativeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CooperativeApplication.class, args);
    }
}
