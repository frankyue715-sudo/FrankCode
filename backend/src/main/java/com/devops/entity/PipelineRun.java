package com.devops.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ci_pipeline_run")
public class PipelineRun {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long pipelineId;
    
    private String pipelineName;
    
    private String commitId;
    
    private String commitMessage;
    
    private String branch;
    
    private Integer status;
    
    private Integer duration;
    
    private String logs;
    
    private String triggerType;
    
    private String triggerUser;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @TableLogic
    private Integer deleted;
}
