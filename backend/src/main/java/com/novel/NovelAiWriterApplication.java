package com.novel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.novel.mapper")
@EnableAsync
public class NovelAiWriterApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelAiWriterApplication.class, args);
    }
}
