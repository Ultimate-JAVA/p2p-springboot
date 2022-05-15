package com.gz.p2p;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDubboConfiguration
@EnableScheduling
public class TimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimerApplication.class, args);
    }

}
