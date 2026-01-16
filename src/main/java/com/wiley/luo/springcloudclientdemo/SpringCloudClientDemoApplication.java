package com.wiley.luo.springcloudclientdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SpringCloudClientDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudClientDemoApplication.class, args);
    }

}
