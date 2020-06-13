package com.gmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class MainApplicationItemWeb {
    public static void main(String[] args) {
        SpringApplication.run(MainApplicationItemWeb.class, args);
    }
}
