package com.devops.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("monitor_alert")
public class Alert {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String target;
    
    private String metric;
    
    private String condition;
    
    private Double threshold;
    
    private Integer severity;
    
    private Integer status;
    
    private String message;
    
    private String notifyType;
    
    private String notifyUsers;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
