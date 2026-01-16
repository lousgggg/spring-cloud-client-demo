package com.wiley.luo.springcloudclientdemo.mapper;

import com.wiley.luo.springcloudclientdemo.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    Member selectById(@Param("id") Long id);
}
