package com.paygateway.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.paygateway.admin.mapper")
public class PayGatewayAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayGatewayAdminApplication.class, args);
    }
}
