package com.gmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class MainApplicationManagerWeb {

    public static void main(String[] args) {
        SpringApplication.run(MainApplicationManagerWeb.class, args);
    }
}
