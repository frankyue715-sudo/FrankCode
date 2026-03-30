package com.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
