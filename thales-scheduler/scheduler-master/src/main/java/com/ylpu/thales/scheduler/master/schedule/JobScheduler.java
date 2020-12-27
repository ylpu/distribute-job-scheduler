package com.ylpu.thales.scheduler.master.schedule;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.enums.MisfirePolicy;

public class JobScheduler {

    private static Log LOG = LogFactory.getLog(JobScheduler.class);

    private static SchedulerFactory schedulerFactory = null;

    private static String QUARTZ_PROPERTIES_PATH = "quartz.properties";

    static {
        init();
    }

    private static void init() {
        try {
            String quartzConfig = System.getProperty("quartz.configurationFile");
            if(StringUtils.isNoneBlank(quartzConfig)) {
                QUARTZ_PROPERTIES_PATH = quartzConfig;
            }
            LOG.info("start to load quartz config " + QUARTZ_PROPERTIES_PATH);
            schedulerFactory = new StdSchedulerFactory(QUARTZ_PROPERTIES_PATH);
        } catch (SchedulerException e) {
            LOG.error(e);
        }
    }

    /**
     * 动态添加任务
     * 
     * @param scheduleInfo
     * @param jobClass
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void addJob(JobScheduleInfo scheduleInfo, Class jobClass) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(scheduleInfo.getJobName(), scheduleInfo.getJobGroupName()).build();
            jobDetail.getJobDataMap().put("id", scheduleInfo.getId());
//            jobDetail.getJobDataMap().put("data", scheduleInfo.getData());
            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity(scheduleInfo.getTriggerName(), scheduleInfo.getTriggerGroupName())
            .withPriority(scheduleInfo.getJobPriority() * 5);
            triggerBuilder.startNow();
            // 触发器时间设定
            CronScheduleBuilder cb = CronScheduleBuilder.cronSchedule(scheduleInfo.getCron());
            setMisfirePolicy(cb);
            triggerBuilder.withSchedule(cb);
            // 创建Trigger对象
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();
            // 调度容器设置JobDetail和Trigger
            sched.scheduleJob(jobDetail, trigger);
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 动态修改任务时间
     * 
     * @param scheduleInfo
     */
    public static void modifyJobTime(JobScheduleInfo scheduleInfo) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(scheduleInfo.getTriggerName(),
                    scheduleInfo.getTriggerGroupName());
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }
            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity(scheduleInfo.getTriggerName(), scheduleInfo.getTriggerGroupName())
            .withPriority(scheduleInfo.getJobPriority() * 5);
            triggerBuilder.startNow();
            
            CronScheduleBuilder cb = CronScheduleBuilder.cronSchedule(scheduleInfo.getCron());
            setMisfirePolicy(cb);
            // 触发器时间设定
            triggerBuilder.withSchedule(cb);
            // 创建Trigger对象
            trigger = (CronTrigger) triggerBuilder.build();
            sched.rescheduleJob(triggerKey, trigger);

        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 动态删除任务
     * 
     * @param scheduleInfo
     */
    public static void removeJob(JobScheduleInfo scheduleInfo) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();

            TriggerKey triggerKey = TriggerKey.triggerKey(scheduleInfo.getTriggerName(),
                    scheduleInfo.getTriggerGroupName());

            sched.pauseTrigger(triggerKey);// 停止触发器
            sched.unscheduleJob(triggerKey);// 移除触发器
            sched.deleteJob(JobKey.jobKey(scheduleInfo.getJobName(), scheduleInfo.getJobGroupName()));// 删除任务
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 判断任务
     * 
     * @param scheduleInfo
     */
    public static boolean jobExists(JobScheduleInfo scheduleInfo) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            return sched.checkExists(JobKey.jobKey(scheduleInfo.getJobName(), scheduleInfo.getJobGroupName()));
        } catch (Exception e) {
            LOG.error(e);
            return false;
        }
    }

    /**
     * 任务下线
     * 
     * @param scheduleInfo
     */
    public static void unscheduleJob(JobScheduleInfo scheduleInfo) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();

            TriggerKey triggerKey = TriggerKey.triggerKey(scheduleInfo.getTriggerName(),
                    scheduleInfo.getTriggerGroupName());

            sched.pauseTrigger(triggerKey);// 停止触发器
            sched.unscheduleJob(triggerKey);// 移除触发器
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description:启动所有定时任务
     */
    public static void startJobs() {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            sched.start();
        } catch (Exception e) {
            LOG.error(e);
            System.exit(1);
        }
    }

    /**
     * @Description:关闭所有定时任务
     */
    public static void shutdownJobs() {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            if (!sched.isShutdown()) {
                sched.shutdown();
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }
    
    private static void setMisfirePolicy(CronScheduleBuilder cb) {
        Properties prop = Configuration.getConfig();
        String misfirePolicy = Configuration.getString(prop, "thales.scheduler.misfire.policy", "ignore");
        MisfirePolicy ms = MisfirePolicy.getMisfirePolicy(misfirePolicy);
        if (ms == MisfirePolicy.IGNORE) {
            cb.withMisfireHandlingInstructionIgnoreMisfires();
        }else if(ms == MisfirePolicy.NOTHING) {
            cb.withMisfireHandlingInstructionDoNothing();
        }else if (ms == MisfirePolicy.PROCEED){
            cb.withMisfireHandlingInstructionFireAndProceed();
        }
    }
}
