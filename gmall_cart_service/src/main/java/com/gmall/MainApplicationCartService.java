package com.gmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.sun.org.glassfish.gmbal.ManagedObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Hello world!
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.gmall.mapper")
public class MainApplicationCartService {
    public static void main(String[] args) {

        SpringApplication.run(MainApplicationCartService.class, args);
    }
}
