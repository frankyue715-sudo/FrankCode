package com.devops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops.entity.Alert;
import com.devops.mapper.AlertMapper;
import org.springframework.stereotype.Service;

@Service
public class AlertService extends ServiceImpl<AlertMapper, Alert> {
    
    public Page<Alert> page(int current, int size, String target, Integer status, Integer severity) {
        Page<Alert> page = new Page<>(current, size);
        QueryWrapper<Alert> wrapper = new QueryWrapper<>();
        if (target != null && !target.isEmpty()) {
            wrapper.like("target", target);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (severity != null) {
            wrapper.eq("severity", severity);
        }
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    public boolean changeStatus(Long id, Integer status) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setStatus(status);
        return this.updateById(alert);
    }
    
    public int getActiveAlertCount() {
        QueryWrapper<Alert> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return this.count(wrapper);
    }
}
