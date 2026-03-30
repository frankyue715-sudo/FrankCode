package com.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops.entity.Alert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlertMapper extends BaseMapper<Alert> {
}
