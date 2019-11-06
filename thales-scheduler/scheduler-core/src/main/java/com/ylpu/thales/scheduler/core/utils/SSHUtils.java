package com.ylpu.thales.scheduler.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ch.ethz.ssh2.Connection;
import com.ylpu.thales.scheduler.core.ssh.SSHClient;

public final class SSHUtils {
      
    private static Log LOG = LogFactory.getLog(SSHUtils.class); 
  
    private SSHUtils(){
      
    }
    
    public static int executeCommand(String server,String userName,String password,String command){     
     
        SSHClient sshClient = SSHClient.getInstance();
        int returnCode = 0;
        try {
             Connection conn = sshClient.getConnection(server,userName,password);
             if(conn != null) {
            	   returnCode = sshClient.execCommand(conn,command.toString()); 
             }else {
            	   throw new RuntimeException("failed connect to server " + server);
             }
                         
        } catch (Exception e) {           
             LOG.error(e);
             returnCode = -1;
        }         
        return returnCode;
    }
    public static void main(String[] args) {
      	System.out.println(executeCommand("127.0.0.1",null,null,"pwd"));
    }
}
