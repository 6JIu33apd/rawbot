package ru.kusupovar.rawbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CryptanalystApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(CryptanalystApplication.class, args);
        } catch (Exception e) {
            log.error("Ошибка запуска: {}", e.getMessage(), e);
        }
    }
}