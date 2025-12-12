package com.dac.chargemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ChargeManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargeManagerApplication.class, args);
    }
}

