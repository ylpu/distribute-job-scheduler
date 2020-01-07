package com.ylpu.thales.scheduler.executor.sql;

import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.core.rpc.entity.JobInstanceRequestRpc;
import com.ylpu.thales.scheduler.core.rpc.entity.JobStatusRequestRpc;
import com.ylpu.thales.scheduler.core.utils.DateUtils;
import com.ylpu.thales.scheduler.core.utils.FileUtils;
import com.ylpu.thales.scheduler.core.utils.JsonUtils;
import com.ylpu.thales.scheduler.core.utils.MetricsUtils;
import com.ylpu.thales.scheduler.enums.SQLOperator;
import com.ylpu.thales.scheduler.enums.TaskState;
import com.ylpu.thales.scheduler.executor.AbstractCommonExecutor;
import com.ylpu.thales.scheduler.request.JobInstanceRequest;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public class SQLExecutor extends AbstractCommonExecutor{ 
	            
    private JobInstanceRequestRpc requestRpc;
    private JobInstanceRequest request;
            
    public SQLExecutor (JobInstanceRequestRpc requestRpc,JobInstanceRequest request){
        super(requestRpc,request);
        this.requestRpc = requestRpc;
        this.request = request;
    }
    
    @Override
    public void execute() throws Exception{
        Properties prop = Configuration.getConfig();
        int logServerPort = Configuration.getInt(prop, "thales.log.server.port", LOG_SERVER_PORT);
        
        String logDir = Configuration.getString(prop, "thales.worker.log.path", DEFAULT_LOG_DIR);
        String logPath = logDir + File.separator + requestRpc.getJob().getId() + "-" + requestRpc.getId() + "-" + 
                DateUtils.getDateAsString(request.getStartTime(),DateUtils.TIME_FORMAT);
        String logOutPath = logPath + ".out";
        request.setLogPath(logOutPath);
        request.setLogUrl("http://" + MetricsUtils.getHostIpAddress() + ":" + logServerPort
                + "/api/log/viewLog/" + requestRpc.getId());
        request.setTaskState(TaskState.RUNNING.getCode());
        
        //修改任务状态
        JobStatusRequestRpc jobStatusRequestRpc = buildJobStatus(requestRpc.getRequestId(),
        		TaskState.RUNNING,request);
        int returnCode = updateJobStatus(jobStatusRequestRpc);
        
        if(returnCode != 200) {
            throw new RuntimeException("failed to update task for " + requestRpc.getId());
        }
        
      	String jobConfig = requestRpc.getJob().getJobConfiguration();
      	if(StringUtils.isNotBlank(jobConfig)) {
      		Connection connection = null;
    		    try {
    	         	SQLConfig sqlConfig = JsonUtils.jsonToBean(jobConfig, SQLConfig.class);
    	          	String operator = sqlConfig.getOperator();
    	          	SQLOperator sqlOperator = SQLOperator.getSQLOperator(operator);
    	    		    connection = ConnectionManager.getConnection(sqlConfig.getConnection());
    	          	switch(sqlOperator) {
    	          	   case SELECT:
    	          		   select(connection,new JdbcExtractor(),sqlConfig.getSql(),sqlConfig.getParameters(),logOutPath);
    	          	       break;
    	          	   case INSERT:
    	          		   execute(connection,sqlConfig.getSql(),sqlConfig.getParameters(),logOutPath);
    	          		   break;
    	          	   case UPDATE:
    	          		   execute(connection,sqlConfig.getSql(),sqlConfig.getParameters(),logOutPath);
    	          		   break;
    	          	   case DELETE:
    	          		   execute(connection,sqlConfig.getSql(),sqlConfig.getParameters(),logOutPath);
    	          		   break;
    	          	   default:
    	          	} 	
    		    }catch(Exception e) {
    		        FileUtils.writeFile("failed to execute task " + request.getId() + " with exception " + e.getMessage(),logOutPath);
    	    	        throw e;
    	        }finally {
    		    	   if(connection != null) {
    		    		  connection.close();
    		    	   }
    		    }
      	}
    }
    
    private void select(Connection conn,JdbcExtractor extractor,String sql, Map<String,Object> map,String logOutPath) throws Exception{
    	    if(conn != null) {
        	    PreparedStatement pstmt = null;
        	    ResultSet rs = null;
        	    try {
        	        pstmt = conn.prepareStatement(sql);
        	    	    if(map != null && map.size() > 0) {
                	   List<Object> mapValueList = new ArrayList<Object>(map.values()); 
                	   for(int i = 0 ; i< mapValueList.size() ; i++) {
                	       pstmt.setObject(i+1, mapValueList.get(i));
                	   }
        	    	    }
        	        rs = pstmt.executeQuery();
        	      	extractor.extract(rs,logOutPath);
        	    }catch(Exception e) {
    		        FileUtils.writeFile("failed to execute task " + request.getId() + " with exception " + e.getMessage(),logOutPath);
        	    	    throw e;
        	    }finally {
        	    	    try {
            	    	    if(pstmt != null) {
         	    	    	   pstmt.close();
         	    	    }
            	    	    if(rs != null) {
          	    	    	   rs.close();
          	    	    }
        	    	    }catch(Exception e1) {
        	    	      	throw e1;
        	    	    }
        	    }
    	    }
    }
   
    private static class JdbcExtractor{
    	   
    	   public void extract(ResultSet rs,String logOutPath) throws Exception {
    		   
    		   ResultSetMetaData metaData = rs.getMetaData();
    		   writeHeader(metaData,logOutPath);
           
           StringBuilder dataBuilder = null;
           int columnCount = metaData.getColumnCount();
           while(rs.next()){
        	       dataBuilder = new StringBuilder(); 
               for (int i = 0; i < columnCount; i++) {
                    String columnName = metaData.getColumnName(i+1);
                    Object object = rs.getObject(columnName);
                    dataBuilder.append(object).append("\t");
               }
               FileUtils.writeFile(dataBuilder.toString(),logOutPath);
               FileUtils.writeFile("\n",logOutPath);
    	       }
        }
    	    private void writeHeader(ResultSetMetaData metaData,String logOutPath) throws Exception {
    			StringBuilder headerBuilder = new StringBuilder(); 
    	        int columnCount = metaData.getColumnCount();
    	        for (int i = 0; i < columnCount; i++) {
    	             String columnName = metaData.getColumnName(i+1);
    	             headerBuilder.append(columnName).append("\t");
    	        }
    	        FileUtils.writeFile(headerBuilder.toString(),logOutPath);
    	        FileUtils.writeFile("\n",logOutPath);
    	    }
    } 	
    
    private void execute(Connection conn,String sql, Map<String,Object> map,String logOutPath) throws Exception{
	    if(conn != null) {
		    PreparedStatement pstmt = null;
		    ResultSet rs = null;
		    try {
		        pstmt = conn.prepareStatement(sql);
		        if(map != null && map.size() > 0) {
			      	List<Object> valueList = new ArrayList<Object>(map.values()); 
			      	for(int i = 0 ; i< valueList.size() ; i++) {
			      		pstmt.setObject(i+1, valueList.get(i));
			      	}
		        }
		        pstmt.execute();
		        FileUtils.writeFile("successful execute task " + request.getId(),logOutPath);
		    }catch(Exception e) {
		        FileUtils.writeFile("failed to execute task " + request.getId() + " with exception " + e.getMessage(),logOutPath);
		    	    throw e;
		    }finally {
		    	    try {
	    	    	    if(pstmt != null) {
	 	    	    	   pstmt.close();
	 	    	    }
	    	    	    if(rs != null) {
	  	    	    	   rs.close();
	  	    	    }
		    	    }catch(Exception e1) {
	    		      FileUtils.writeFile("failed to execute task " + request.getId() + " with exception " + e1.getMessage(),logOutPath);
		    	    	  throw e1;
		    	    }
		    }
	    }
    }
    
    /**
     * 
     */
    @Override
    public void kill() throws Exception{

    }
    
    /**
     * {"datasource" : "","operator":"","sql":"","parameters" : ""}
     * @param configFile
     * @return
     */
    @Override
    public String[] buildCommand(String configFile) throws Exception {
       return null;
    }

	@Override
	public void preExecute() throws Exception {
		
	}

	@Override
	public void postExecute() throws Exception {
		
	}
}
