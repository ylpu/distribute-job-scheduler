package com.ylpu.thales.scheduler.rest.resources;

import com.ylpu.thales.scheduler.core.utils.SSHUtils;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Path("worker")
public class SchedulerWorkerResource {
    
    private static Log LOG = LogFactory.getLog(SchedulerWorkerResource.class);

    @POST
    @Path("down")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void downWorker(WorkerRequest request) throws Exception {
        String command = "ps -ef | grep WorkerServer | grep -v grep | awk '{print $2}'|xargs kill -9";
        int returnCode = SSHUtils.executeCommand(request.getHost(), null, null, command);
        if(returnCode != 0) {
            String errorMsg = "failed to kill worker " + request.getHost() + ";" + request.getPort();
            LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }
}
