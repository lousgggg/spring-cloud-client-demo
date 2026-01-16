package com.wiley.luo.springcloudclientdemo.config;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void init() {
        // 初始化Sentinel配置
    }

    // 配置统一异常处理
//    @Bean
//    public SentinelResourceAspect sentinelResourceAspect() {
//        return new SentinelResourceAspect();
//    }
}
