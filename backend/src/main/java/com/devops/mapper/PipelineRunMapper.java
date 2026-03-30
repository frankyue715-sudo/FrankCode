package com.devops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops.entity.PipelineRun;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineRunMapper extends BaseMapper<PipelineRun> {
}
