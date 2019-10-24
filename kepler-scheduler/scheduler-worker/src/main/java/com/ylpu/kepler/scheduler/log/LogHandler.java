package com.ylpu.kepler.scheduler.log;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

import com.ylpu.kepler.scheduler.core.rest.JobManager;
import com.ylpu.kepler.scheduler.core.utils.ScriptUtils;
import com.ylpu.kepler.scheduler.response.JobInstanceResponse;

@Path("log")
public class LogHandler {
    @GET
    @Path("viewLog/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String viewLog(@PathParam("taskId") int taskId) throws Exception {
        JobInstanceResponse jobInstanceResponse = JobManager.getJobInstanceById(taskId);
        String logPath = jobInstanceResponse.getLogPath();
        if(StringUtils.isNotBlank(logPath)) {
            return ScriptUtils.execToString("cat",jobInstanceResponse.getLogPath(), new String[] {}); 
        }
        return "";
    }
}
