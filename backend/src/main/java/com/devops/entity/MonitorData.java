package com.devops.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("monitor_data")
public class MonitorData {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String target;
    
    private String targetType;
    
    private Double cpuUsage;
    
    private Double memoryUsage;
    
    private Double diskUsage;
    
    private Double networkIn;
    
    private Double networkOut;
    
    private Integer processCount;
    
    private Double responseTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
