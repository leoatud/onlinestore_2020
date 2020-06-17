package com.gmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Hello world!
 */
@SpringBootApplication
@EnableDubbo
@EnableTransactionManagement
@MapperScan(basePackages = "com.gmall.mapper")
public class MainApplicationOrderService {
    public static void main(String[] args) {
        SpringApplication.run(MainApplicationOrderService.class, args);
    }
}
