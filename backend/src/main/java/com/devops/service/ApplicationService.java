package com.devops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops.entity.Application;
import com.devops.mapper.ApplicationMapper;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService extends ServiceImpl<ApplicationMapper, Application> {
    
    public Page<Application> page(int current, int size, String name) {
        Page<Application> page = new Page<>(current, size);
        QueryWrapper<Application> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    public boolean changeStatus(Long id, Integer status) {
        Application app = new Application();
        app.setId(id);
        app.setStatus(status);
        return this.updateById(app);
    }
}
