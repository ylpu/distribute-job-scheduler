package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import com.ylpu.thales.scheduler.enums.TaskState;
import lombok.Data;

@Data
public class JobInstanceResponse implements Serializable {
    
    private Integer id;

    private JobResponse jobConf;
    
    private String jobName;
    
    private Integer jobId;

    private TaskState taskState;

    private String logUrl;

    private String logPath;
    
    private String worker;

    private String creatorName;

    private String creatorEmail;

    private Integer retryTimes;

    private Integer pid;

    private String applicationid;
    
    private String scheduleTime;
    
    private String startTime;
    
    private String endTime;

    private Integer elapseTime;

    private static final long serialVersionUID = 1L;
    
}