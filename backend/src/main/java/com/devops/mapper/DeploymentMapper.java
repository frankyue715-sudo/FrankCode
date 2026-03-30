package com.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops.entity.Deployment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeploymentMapper extends BaseMapper<Deployment> {
}
