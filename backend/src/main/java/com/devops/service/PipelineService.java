package com.devops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops.entity.Pipeline;
import com.devops.mapper.PipelineMapper;
import org.springframework.stereotype.Service;

@Service
public class PipelineService extends ServiceImpl<PipelineMapper, Pipeline> {
    
    public Page<Pipeline> page(int current, int size, String name) {
        Page<Pipeline> page = new Page<>(current, size);
        QueryWrapper<Pipeline> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    public boolean changeStatus(Long id, Integer status) {
        Pipeline pipeline = new Pipeline();
        pipeline.setId(id);
        pipeline.setStatus(status);
        return this.updateById(pipeline);
    }
}
