package com.sample.leak.project;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootConfiguration
@SpringBootApplication(
        scanBasePackages = {
                "com.sample.leak.project"},
        exclude = {
                DataSourceAutoConfiguration.class,
                LiquibaseAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
public class SampleApplication {

        public static void main(String[] args) {
                new SpringApplicationBuilder()
                        .sources(SampleApplication.class)
                        .run(args);
        }
}
