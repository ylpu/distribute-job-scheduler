package com.ylpu.thales.scheduler.master.api.resources;

import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.master.api.service.SchedulerService;
import com.ylpu.thales.scheduler.request.ScheduleRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("job")
public class SchedulerJobResource {

    @POST
    @Path("kill")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void killJob(ScheduleRequest request) throws Exception {
        new SchedulerService().killJob(request);
    }

    @POST
    @Path("schedule")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void scheduleJob(ScheduleRequest request) throws Exception {
        new SchedulerService().scheduleJob(request);
    }

    @POST
    @Path("reschedule")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void rescheduleJob(ScheduleRequest request) throws Exception {
        new SchedulerService().rescheduleJob(request);
    }

    @POST
    @Path("down")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void downJob(ScheduleRequest request) throws Exception {
        new SchedulerService().downJob(request);
    }

    @POST
    @Path("rerun")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void rerun(ScheduleRequest request) throws Exception {
        new SchedulerService().rerun(request.getId());
    }

    @POST
    @Path("rerunAll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void rerunAll(ScheduleRequest request) throws Exception {
        new SchedulerService().rerunAll(request.getId());
    }

    @POST
    @Path("markSuccess")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void markSuccess(ScheduleRequest request) throws Exception {
        new SchedulerService().markStatus(request, TaskState.SUCCESS);
    }

    @POST
    @Path("markFail")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void markFail(ScheduleRequest request) throws Exception {
        new SchedulerService().markStatus(request, TaskState.FAIL);
    }
}
