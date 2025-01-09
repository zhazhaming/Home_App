package com.home;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author: zhazhaming
 * @Date: 2024/05/03/15:42
 */
@SpringBootApplication
@MapperScan("com.home.mapper")
//@ComponentScan(basePackages = {"com.home.utils"})
public class Movie_Application {
    public static void main(String[] args) {
        SpringApplication.run(Movie_Application.class,args);
    }
}
