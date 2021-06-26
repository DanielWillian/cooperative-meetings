package org.cooperative.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"org.cooperative"})
@ComponentScan(basePackages = {"org.cooperative"})
@EnableJpaRepositories(basePackages = {"org.cooperative"})
public class CooperativeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CooperativeApplication.class, args);
    }
}
