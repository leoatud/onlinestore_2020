package com.gmall.order;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class MainApplicationOrderWeb {

    public static void main(String[] args) {
        SpringApplication.run(MainApplicationOrderWeb.class, args);
    }
}
