package com.carl.tu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.carl"})
@EnableEurekaClient
@MapperScan("com.carl.tu.dao")
@SpringBootApplication(scanBasePackages = "com.carl")
public class TuApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuApplication.class, args);
    }

}
