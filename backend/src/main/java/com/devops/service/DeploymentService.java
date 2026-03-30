package com.devops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops.entity.Application;
import com.devops.entity.Deployment;
import com.devops.mapper.DeploymentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeploymentService extends ServiceImpl<DeploymentMapper, Deployment> {
    
    public Page<Deployment> page(int current, int size, Long applicationId, String environment) {
        Page<Deployment> page = new Page<>(current, size);
        QueryWrapper<Deployment> wrapper = new QueryWrapper<>();
        if (applicationId != null) {
            wrapper.eq("application_id", applicationId);
        }
        if (environment != null && !environment.isEmpty()) {
            wrapper.eq("environment", environment);
        }
        wrapper.orderByDesc("start_time");
        return this.page(page, wrapper);
    }
    
    public Deployment startDeployment(Application app, String environment, String version, String deployedBy) {
        Deployment deployment = new Deployment();
        deployment.setApplicationId(app.getId());
        deployment.setApplicationName(app.getName());
        deployment.setEnvironment(environment);
        deployment.setVersion(version);
        deployment.setImage(app.getImage());
        deployment.setStatus(1);
        deployment.setDeployedBy(deployedBy);
        deployment.setStartTime(LocalDateTime.now());
        this.save(deployment);
        
        deployment.setStatus(3);
        deployment.setEndTime(LocalDateTime.now());
        deployment.setReason("Deployment completed successfully");
        this.updateById(deployment);
        
        return deployment;
    }
    
    public boolean rollback(Long deploymentId) {
        Deployment deployment = this.getById(deploymentId);
        if (deployment == null) return false;
        
        deployment.setStatus(4);
        deployment.setReason("Rollback initiated");
        deployment.setEndTime(LocalDateTime.now());
        return this.updateById(deployment);
    }
}
