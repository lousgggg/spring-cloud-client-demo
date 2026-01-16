package com.wiley.luo.springcloudclientdemo.service.impl;

import com.wiley.luo.springcloudclientdemo.entity.Member;
import com.wiley.luo.springcloudclientdemo.mapper.MemberMapper;
import com.wiley.luo.springcloudclientdemo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public Member getMemberById(Long id) {
        memberMapper.selectById(id);
        return null;
    }
}
