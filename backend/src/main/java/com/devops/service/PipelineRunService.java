package com.devops.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops.entity.Pipeline;
import com.devops.entity.PipelineRun;
import com.devops.mapper.PipelineRunMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PipelineRunService extends ServiceImpl<PipelineRunMapper, PipelineRun> {
    
    public Page<PipelineRun> page(int current, int size, Long pipelineId, Integer status) {
        Page<PipelineRun> page = new Page<>(current, size);
        QueryWrapper<PipelineRun> wrapper = new QueryWrapper<>();
        if (pipelineId != null) {
            wrapper.eq("pipeline_id", pipelineId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("start_time");
        return this.page(page, wrapper);
    }
    
    public PipelineRun startRun(Pipeline pipeline, String triggerType, String triggerUser) {
        PipelineRun run = new PipelineRun();
        run.setPipelineId(pipeline.getId());
        run.setPipelineName(pipeline.getName());
        run.setBranch(pipeline.getBranch());
        run.setCommitId(UUID.randomUUID().toString().substring(0, 8));
        run.setCommitMessage("Triggered by " + triggerType);
        run.setStatus(1);
        run.setTriggerType(triggerType);
        run.setTriggerUser(triggerUser);
        run.setStartTime(LocalDateTime.now());
        this.save(run);
        
        run.setLogs("[" + LocalDateTime.now() + "] Build started...\n");
        this.updateById(run);
        
        return run;
    }
    
    public boolean updateRunStatus(Long runId, Integer status, String logs) {
        PipelineRun run = this.getById(runId);
        if (run == null) return false;
        
        run.setStatus(status);
        run.setEndTime(LocalDateTime.now());
        run.setDuration((int) java.time.Duration.between(run.getStartTime(), run.getEndTime()).getSeconds());
        
        if (logs != null) {
            run.setLogs(run.getLogs() + "\n" + logs);
        }
        
        return this.updateById(run);
    }
}
