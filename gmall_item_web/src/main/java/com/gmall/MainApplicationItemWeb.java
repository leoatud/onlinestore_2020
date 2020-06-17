package com.gmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan(basePackages = "com.gmall")
public class MainApplicationItemWeb {
    public static void main(String[] args) {
        SpringApplication.run(MainApplicationItemWeb.class, args);
    }
}
