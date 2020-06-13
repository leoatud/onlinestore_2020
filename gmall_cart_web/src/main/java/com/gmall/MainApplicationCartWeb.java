package com.gmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableDubbo
public class MainApplicationCartWeb {
    public static void main(String[] args) {

        SpringApplication.run(MainApplicationCartWeb.class, args);
    }
}
