package com.gmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableDubbo
@MapperScan(basePackages = "com.gmall.mapper")
public class MainApplicationPayment
{
    public static void main( String[] args )
    {
        SpringApplication.run(MainApplicationPayment.class,args);
        System.out.println( "Hello World!" );
    }
}
