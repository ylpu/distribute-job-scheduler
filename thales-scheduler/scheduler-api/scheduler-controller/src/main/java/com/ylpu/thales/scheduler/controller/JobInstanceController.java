package com.ylpu.thales.scheduler.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.github.pagehelper.PageInfo;
import com.ylpu.thales.scheduler.common.utils.StreamUtils;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import com.ylpu.thales.scheduler.request.JobStatusRequest;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;
import com.ylpu.thales.scheduler.response.JobInstanceStateResponse;
import com.ylpu.thales.scheduler.response.SchedulerResponse;
import com.ylpu.thales.scheduler.response.TaskElapseChartResponse;
import com.ylpu.thales.scheduler.response.TaskSummaryResponse;
import com.ylpu.thales.scheduler.response.WorkerSummaryResponse;
import com.ylpu.thales.scheduler.service.JobInstanceService;
import com.ylpu.thales.scheduler.service.exception.ThalesRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.*;

@Controller
@RequestMapping("/api/jobInstance")
public class JobInstanceController {

    @Autowired
    private JobInstanceService jobInstanceService;

    @ResponseBody
    @RequestMapping(value = "/addJobInstance", method = RequestMethod.POST)
    public SchedulerResponse<Integer> addJobInstance(@RequestBody JobInstanceRequest request) {
        return new SchedulerResponse<Integer>(jobInstanceService.addJobInstance(request));
    }

    @ResponseBody
    @RequestMapping(value = "/updateJobInstanceSelective", method = RequestMethod.POST)
    public SchedulerResponse<Void> updateJobInstanceSelective(@RequestBody JobInstanceRequest request) {
        jobInstanceService.updateJobInstanceSelective(request);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/updateJobInstanceByKey", method = RequestMethod.POST)
    public SchedulerResponse<Void> updateJobInstanceByKey(@RequestBody JobInstanceRequest request) {
        jobInstanceService.updateJobInstanceByKey(request);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/getJobInstanceById", method = RequestMethod.GET)
    public SchedulerResponse<JobInstanceResponse> getJobInstanceById(@RequestParam("id") Integer id) {
        return new SchedulerResponse<JobInstanceResponse>(jobInstanceService.getJobInstanceById(id));
    }

    @ResponseBody
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public SchedulerResponse<PageInfo<JobInstanceResponse>> paging(
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "taskState", required = false) Integer taskState,
            @RequestParam(value = "jobName", required = false) String jobName) {
        return new SchedulerResponse<PageInfo<JobInstanceResponse>>(
                jobInstanceService.findAll(taskState, jobName, pageNo, pageSize));
    }

    @ResponseBody
    @RequestMapping(value = "/getInstanceIdByTime", method = RequestMethod.GET)
    public SchedulerResponse<JobInstanceResponse> getInstanceIdByTime(@RequestParam("jobId") Integer jobId,
            @RequestParam("scheduleTime") String scheduleTime) {
        return new SchedulerResponse<JobInstanceResponse>(jobInstanceService.getInstanceIdByTime(jobId, scheduleTime));
    }

    /**
     * master启动时获取每台机器运行中的任务个数，供监控使用
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getRunningJobCount", method = RequestMethod.GET)
    public SchedulerResponse<List<Map<String, Object>>> getRunningJobCount() {
        return new SchedulerResponse<List<Map<String, Object>>>(jobInstanceService.getRunningJobCount());
    }

    /**
     * master启动时加载最近一个月任务实例状态(可以根据实际情况调整)
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getAllJobStatus", method = RequestMethod.GET)
    public SchedulerResponse<List<JobInstanceStateResponse>> getAllJobStatus() {
        return new SchedulerResponse<List<JobInstanceStateResponse>>(jobInstanceService.getAllJobStatus());
    }

    @ResponseBody
    @RequestMapping(value = "/viewLog", method = RequestMethod.GET)
    public void viewLog(@RequestParam(value = "logUrl", required = false) String logUrl) {
        // ParameterizedTypeReference<String> typeRef = new
        // ParameterizedTypeReference<String>() {};
        // String str = "";
        // try {
        // str = RestClient.getForObject(logPath,typeRef,null);
        // }catch(Exception e) {
        // LOG.error(e);
        // throw new ThalesRuntimeException("无法查看日志");
        // }
        // return new SchedulerResponse<String>(str);

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse();
        ServletOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            StreamUtils.writeOutput(inputStream, outputStream, response, logUrl);
        } catch (Exception e) {
            throw new ThalesRuntimeException("无法查看日志", e);
        } finally {
            if (outputStream != null) {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    throw new ThalesRuntimeException("无法查看日志", e);
                }
            }
        }
    }

    /**
     * 杀任务
     * 
     * @param request
     */
    @ResponseBody
    @RequestMapping(value = "/kill", method = RequestMethod.POST)
    public SchedulerResponse<Void> killJob(@Validated @RequestBody ScheduleRequest request, HttpSession session) {
        Object object = session.getAttribute("user");
        jobInstanceService.killJob(request, object);
        return SchedulerResponse.success();
    }

    /**
     * 重跑当前任务实例
     * 
     * @param request
     */
    @ResponseBody
    @RequestMapping(value = "/rerun", method = RequestMethod.POST)
    public SchedulerResponse<Void> rerun(@Validated @RequestBody ScheduleRequest request, HttpSession session) {
        Object object = session.getAttribute("user");
        jobInstanceService.rerun(request, object);
        return SchedulerResponse.success();
    }

    /**
     * 重跑当前及下游所有任务实例
     * 
     * @param request
     */
    @ResponseBody
    @RequestMapping(value = "/rerunAll", method = RequestMethod.POST)
    public SchedulerResponse<Void> rerunAll(@Validated @RequestBody ScheduleRequest request, HttpSession session) {
        Object object = session.getAttribute("user");
        jobInstanceService.rerunAll(request, object);
        return SchedulerResponse.success();
    }

    /**
     * master意外down掉时使用
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/markStatus", method = RequestMethod.POST)
    public SchedulerResponse<Void> markStatus() {
        jobInstanceService.markStatus();
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/markSuccess", method = RequestMethod.POST)
    public SchedulerResponse<Void> markSuccess(@Validated @RequestBody ScheduleRequest request, HttpSession session) {
        Object object = session.getAttribute("user");
        jobInstanceService.markSuccess(request, object);
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/markFail", method = RequestMethod.POST)
    public SchedulerResponse<Void> markFail(@Validated @RequestBody ScheduleRequest request, HttpSession session) {
        Object object = session.getAttribute("user");
        jobInstanceService.markFail(request, object);
        return SchedulerResponse.success();
    }

    /**
     * master意外down掉时使用
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/batchRerun", method = RequestMethod.POST)
    public SchedulerResponse<Void> batchRerun(@RequestParam("ids") String ids, HttpSession session) {
        Object object = session.getAttribute("user");
        if (StringUtils.isNotBlank(ids)) {
            String[] idArray = ids.split(",");
            for (String id : idArray) {
                ScheduleRequest request = new ScheduleRequest();
                request.setId(NumberUtils.toInt(id));
                jobInstanceService.rerun(request, object);
            }
        }
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/updateJobStatus", method = RequestMethod.POST)
    public SchedulerResponse<Void> updateJobStatus(@Validated @RequestBody JobStatusRequest request) {
        jobInstanceService.updateJobStatus(request.getIds(), request.getStatus());
        return SchedulerResponse.success();
    }

    @ResponseBody
    @RequestMapping(value = "/getTaskSummary", method = RequestMethod.GET)
    public SchedulerResponse<List<TaskSummaryResponse>> getTaskSummary() {
        return new SchedulerResponse<List<TaskSummaryResponse>>(jobInstanceService.getTaskSummary());
    }
    
    @ResponseBody
    @RequestMapping(value = "/getWorkerSummary", method = RequestMethod.GET)
    public SchedulerResponse<List<WorkerSummaryResponse>> getWorkerSummary() {
        return new SchedulerResponse<List<WorkerSummaryResponse>>(jobInstanceService.getWorkerSummary());
    }

    @ResponseBody
    @RequestMapping(value = "/getTaskLineByJobId/{id}", method = RequestMethod.GET)
    public SchedulerResponse<List<TaskElapseChartResponse>> getTaskLineByJobId(@PathVariable("id") Integer id) {
        return new SchedulerResponse<List<TaskElapseChartResponse>>(jobInstanceService.getTaskLineByJobId(id));
    }
}