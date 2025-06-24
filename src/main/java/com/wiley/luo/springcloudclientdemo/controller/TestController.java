package com.wiley.luo.springcloudclientdemo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Value("${word}")
    private String word;

    @GetMapping("/hello")
    public String index(@RequestParam String name) {
        return name+","+this.word;
    }

}
