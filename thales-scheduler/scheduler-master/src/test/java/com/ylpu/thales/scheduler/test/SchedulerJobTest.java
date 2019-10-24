package com.ylpu.thales.scheduler.test;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SchedulerJobTest implements Job{
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("schedule time is " + context.getScheduledFireTime() + context.getJobDetail().getKey().getName());
        System.out.println("fire time is " + context.getFireTime() + context.getJobDetail().getKey().getName());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("job end " + context.getJobDetail().getKey().getName());
    }
}
