package com.wiley.luo.springcloudclientdemo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Member {
    private String id;
    private String username;
    private Integer levelId;
    private String nickname;
    private String mobile;
    private String email;
    private String password;
    private String header;
    private Date birth;
    private Integer gender;
    private String city;
    private String job;
    private String sign;
    private Integer sourceType;
    private Integer integration;
    private Integer growth;
    private Integer status;
    private Date createTime;
    private Date socialUid;
    private String accessToken;
    private String expiresIn;

}
