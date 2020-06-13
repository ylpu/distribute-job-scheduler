package com.ylpu.thales.scheduler.log;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;

@Path("log")
public class LogHandler {

    @GET
    @Path("viewLog/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/octet-stream")
    // @Produces("application/json;charset=gbk")
    public Response viewLog(@PathParam("taskId") int taskId) throws Exception {
        JobInstanceResponse jobInstanceResponse = JobManager.getJobInstanceById(taskId);
        String logPath = jobInstanceResponse.getLogPath();
        if (StringUtils.isNotBlank(logPath)) {
            return Response.ok(new LogStreamingOutput(logPath)).build();
        }
        return null;
    }
}
