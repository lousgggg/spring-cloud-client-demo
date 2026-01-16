package com.wiley.luo.springcloudclientdemo.controller;

import com.wiley.luo.springcloudclientdemo.entity.Member;
import com.wiley.luo.springcloudclientdemo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
@RequestMapping("/test")
public class TestController {

    @Value("${word}")
    private String word;
    @Autowired
    private MemberService memberService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/hello")
    public String index(@RequestParam String name) {
        String key = "word:" + name;
        String res = stringRedisTemplate.opsForValue().get(key);
        if (res != null) {
            return res + " from redis!!";
        }
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Member member = memberService.getMemberById(1L);
        if (member == null) {
            res = "hey " + name + "," + word;
            stringRedisTemplate.opsForValue().set(key, res);
            return res;
        }
        return member.getUsername() + "," + this.word;
    }

}
