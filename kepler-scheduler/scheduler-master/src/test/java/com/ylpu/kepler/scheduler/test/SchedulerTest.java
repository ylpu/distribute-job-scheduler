package com.ylpu.kepler.scheduler.test;

import org.junit.Test;

import com.ylpu.kepler.scheduler.manager.JobScheduler;
import com.ylpu.kepler.scheduler.response.JobResponse;
import com.ylpu.kepler.scheduler.rest.entity.JobScheduleInfo;

public class SchedulerTest {
    
    @Test
    public void jobSchedulerTest() throws Exception {
        
        JobResponse jr = new JobResponse();
        jr.setId(1);
        jr.setWorkerGroupname("hive");
        JobScheduleInfo ji = new JobScheduleInfo();
        ji.setJobName("hivetest36");
        ji.setJobGroupName("hive_group3");
        ji.setTriggerName("trigger36");
        ji.setTriggerGroupName("hive_group3");
        ji.setCron("*/5 * * * * ?");
        ji.setData(jr);
        JobScheduler.addJob(ji, SchedulerJobTest.class);
//        Thread.sleep(10000);
//        System.out.println("start to unschedule job");
//        JobScheduler.removeJob(ji);
//        
//        Thread.sleep(10000);
//        System.out.println("start to resume job");
//        JobScheduler.addJob(ji, SchedulerJobTest.class);
        
        
        while(true) {
        }
    }
}   
