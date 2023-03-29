package ru.kusupovar.rawbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class RawbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RawbotApplication.class, args);
    }

}
