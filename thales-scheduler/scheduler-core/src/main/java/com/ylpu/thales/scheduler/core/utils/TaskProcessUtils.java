package com.ylpu.thales.scheduler.core.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.ylpu.thales.scheduler.core.enums.OSInfo;

public class TaskProcessUtils {
	
    public static Long getLinuxPid(Process process) throws Exception{
        long pid = -1;
        Class<?>  clazz = Class.forName("java.lang.UNIXProcess");
        Field field = clazz.getDeclaredField("pid");
        field.setAccessible(true);
        pid = (Integer) field.get(process);
        return pid;
      }
    
     public static Long getWindowsPid(Process process) throws Exception{
         Field f =process.getClass().getDeclaredField("handle");
         f.setAccessible(true);
         long handl =f.getLong(process);
         Kernel32 kernel = Kernel32.INSTANCE;
         WinNT.HANDLE handle = new WinNT.HANDLE();
         handle.setPointer(Pointer.createConstant(handl));
         int ret =kernel.GetProcessId(handle);
         long pid = Long.valueOf(ret);
         return pid;
     }
     
    public static void killLinuxProcess(Long pid) throws Exception{    	 
        String command = "kill -9 " +  pid;
        Runtime rt = Runtime.getRuntime();
        rt.exec(command);
     }
    
    public static void killYarnApplication(String applicationId) throws Exception{      
        String command = "yarn application -kill " + applicationId;
        Runtime rt = Runtime.getRuntime();
        rt.exec(command);
     }
     
     public static void killProcess(OSInfo osinfo,String pid) throws Exception{
         if(StringUtils.isNotBlank(pid)) {
            if(osinfo == OSInfo.Windows) {
               killWindowProcess(Long.valueOf(pid));  		
            }else if(osinfo == OSInfo.Linux) {
               killLinuxProcess(Long.valueOf(pid));    	     		    		
            }
         } 
     }
	
     public static void killWindowProcess(Long pid) throws Exception{
         System.out.println("kill process with id " + pid);
         String cmd =getKillProcessTreeCmd(pid);
         Runtime rt =Runtime.getRuntime();
         Process killPrcess = rt.exec(cmd); 
         killPrcess.waitFor();
         killPrcess.destroy();
     }
     
     public static int execCommand(String fileName,String stdoutPath,String stdErrorPath,Object... params) throws Exception{
         Runtime rt = Runtime.getRuntime();
         StringBuilder sb = new StringBuilder("sh " + fileName + " ");
         if(params != null && params.length > 0) {
             List<Object> list = Arrays.asList(params);
             for(Iterator<Object> it =  list.iterator();it.hasNext();) {
                 Object obj = it.next();
                 sb.append(obj.toString());
                 if(it.hasNext()) {
                     sb.append("");
                  }
             }
         }
         Process process = rt.exec(sb.toString());
         FileUtils.writeFile(process.getInputStream(),stdoutPath);
         FileUtils.writeFile(process.getErrorStream(),stdErrorPath);
         int returnCode = process.waitFor();
         return returnCode;
     }  

     private static String getKillProcessTreeCmd(Long Pid){
         String result = "";
         if(Pid !=null)
            result ="c:/windows/system32/cmd.exe /c taskkill /PID "+ Pid + " /F /T ";
            return result;
     }
	    
     public static void main(String[] args){
//         String fileName = "/tmp/script/test.sh";
//         Runtime rt = Runtime.getRuntime();
//         Process process = rt.exec("sh " + fileName);
//         IOUtils.writeFile(process.getInputStream(),"/tmp/stdout.txt");
//         IOUtils.writeFile(process.getErrorStream(),"/tmp/stderror.txt");
//         System.out.println(process.waitFor());
         try {
//             killLinuxProcess(3l); 
             int pid = 65633;
             TaskProcessUtils.execCommand("/Users/admin/thales/thales-scheduler/scheduler-worker/src/script/kill.sh", 
                     "/tmp/pid/"+ pid+ ".out", "/tmp/pid/"+ pid + ".error", String.valueOf(pid));
         }catch(Exception e) {
             e.printStackTrace();
         }
         
     }
}