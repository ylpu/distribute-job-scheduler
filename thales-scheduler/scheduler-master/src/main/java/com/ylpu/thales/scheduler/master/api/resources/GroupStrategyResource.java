package com.ylpu.thales.scheduler.master.api.resources;

import com.ylpu.thales.scheduler.core.utils.SSHUtils;
import com.ylpu.thales.scheduler.master.strategy.WorkerGroupStrategy;
import com.ylpu.thales.scheduler.request.GroupStrategyRequest;
import com.ylpu.thales.scheduler.request.WorkerRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Path("group")
public class GroupStrategyResource {
    
    private static Log LOG = LogFactory.getLog(SchedulerWorkerResource.class);

    @POST
    @Path("addGroupStrategy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void addGroupStrategy(GroupStrategyRequest request) throws Exception {
        WorkerGroupStrategy.addOrUpdateGroupStrategy(request);
    }
    
    @POST
    @Path("updateGroupStrategy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateGroupStrategy(GroupStrategyRequest request) throws Exception {
        WorkerGroupStrategy.addOrUpdateGroupStrategy(request);
    }
}

