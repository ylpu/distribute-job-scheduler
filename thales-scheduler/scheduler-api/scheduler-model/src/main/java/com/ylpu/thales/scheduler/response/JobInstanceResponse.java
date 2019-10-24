package com.ylpu.thales.scheduler.response;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class JobInstanceResponse implements Serializable {
    
    private Integer id;

    private JobResponse jobConf;

    private Integer taskState;

    private String logUrl;

    private String logPath;
    
    private String worker;

    private String creatorName;

    private String creatorEmail;

    private Integer retryTimes;

    private Integer pid;

    private String applicationid;
    
    private Date scheduleTime;
    
    private Date startTime;
    
    private Date endTime;

    private Integer elapseTime;

    private static final long serialVersionUID = 1L;
    
}