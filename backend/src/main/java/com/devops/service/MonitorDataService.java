package com.devops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops.entity.MonitorData;
import com.devops.mapper.MonitorDataMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class MonitorDataService extends ServiceImpl<MonitorDataMapper, MonitorData> {
    
    private final Random random = new Random();
    
    public Page<MonitorData> page(int current, int size, String target, String targetType) {
        Page<MonitorData> page = new Page<>(current, size);
        QueryWrapper<MonitorData> wrapper = new QueryWrapper<>();
        if (target != null && !target.isEmpty()) {
            wrapper.like("target", target);
        }
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq("target_type", targetType);
        }
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
    
    public List<MonitorData> getLatestByTarget(String target) {
        QueryWrapper<MonitorData> wrapper = new QueryWrapper<>();
        wrapper.eq("target", target);
        wrapper.orderByDesc("create_time");
        wrapper.last("LIMIT 100");
        return this.list(wrapper);
    }
    
    public void collectMockData(String target, String targetType) {
        MonitorData data = new MonitorData();
        data.setTarget(target);
        data.setTargetType(targetType);
        data.setCpuUsage(random.nextDouble() * 100);
        data.setMemoryUsage(random.nextDouble() * 100);
        data.setDiskUsage(30 + random.nextDouble() * 20);
        data.setNetworkIn(random.nextDouble() * 1000);
        data.setNetworkOut(random.nextDouble() * 1000);
        data.setProcessCount(random.nextInt(200) + 50);
        data.setResponseTime(random.nextDouble() * 500);
        data.setCreateTime(LocalDateTime.now());
        this.save(data);
    }
}
