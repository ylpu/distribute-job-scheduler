package com.ylpu.thales.scheduler.log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.core.utils.ScriptUtils;
import com.ylpu.thales.scheduler.response.JobInstanceResponse;

@Path("log")
public class LogHandler {
	
	private static Log LOG = LogFactory.getLog(LogHandler.class);
	
	private static final int LOG_LINES = 100000;
	
    @GET
    @Path("viewLog/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/octet-stream")
//    @Produces("application/json;charset=gbk")
    public Response viewLog(@PathParam("taskId") int taskId) throws Exception {
        JobInstanceResponse jobInstanceResponse = JobManager.getJobInstanceById(taskId);
        String logPath = jobInstanceResponse.getLogPath();
        if(StringUtils.isNotBlank(logPath)) {
//        	  String log = ScriptUtils.execToList("cat",jobInstanceResponse.getLogPath(), new String[] {});
//            return log; 
            return Response.ok(new LogStreamingOutput(logPath)).build();
        }
        return null;
    }
    
    public static class LogStreamingOutput implements StreamingOutput {
    	
    	   private String filePath = "";
    	
    	    public LogStreamingOutput(String filePath) {
    	    	   this.filePath = filePath;
    	    }
        @Override
        public void write(OutputStream output)
                throws IOException, WebApplicationException {
            FileInputStream in = null;  
            BufferedReader br = null;
            try {  
               Properties prop = Configuration.getConfig();
               int logLines = Configuration.getInt(prop,"thales.worker.log.lines",LOG_LINES);
    	           String[] strs = new String[3];
    	           strs[0] = "/bin/bash";
    	           strs[1] = "-c";
    	           strs[2] = "tail -" + logLines + " " + filePath;
    	           Process process = Runtime.getRuntime().exec(strs);
        	       String str = null;
        	       br = new BufferedReader(new InputStreamReader(process.getInputStream()),81920);      	    
        	       while((str = br.readLine()) != null) {
        	    	     str = str + "<br>";
        	    	     output.write(str.getBytes());
        	      }            
            } catch (Exception e) {  
             	LOG.error(e);
                throw e;
            }finally{  
                try {  
                    if(in != null) in.close(); 
                    if(br != null) br.close();
                } catch (Exception e2) { 
                	    LOG.error(e2);
                	    throw e2;
                }          
            }  
        }
    }
}

