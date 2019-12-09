package com.ylpu.thales.scheduler.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ylpu.thales.scheduler.core.constants.GlobalConstants;

public final class FileUtils {
    
    private static final Log LOG = LogFactory.getLog(FileUtils.class);
    
    private static final Pattern APPLICATION_REGEX = Pattern.compile(GlobalConstants.APPLICATION_REGEX);
    
    private FileUtils() {
    	
    }
    
    public static void writeOuput(InputStream is,String fileName){        
        new Thread(new Runnable() {
          public void run() {
        	  writeFile(is,fileName);
        }
      }).start();
    }
    
   public static void writeFile(InputStream is,String fileName) {
	   BufferedWriter bw = null;
       try {
	       File file = new File(fileName);
	       String parentPath = file.getParent();
	       File parentfile = new File(parentPath);
	       if (!parentfile.exists()) {
	    	   parentfile.mkdirs();
	       }
           bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream (fileName,true),"GBK"));
           BufferedReader brError = new BufferedReader(new InputStreamReader(is, "GBK"));
           String errline = null;
           while ((errline = brError.readLine()) != null) {
               bw.write(errline);
               bw.write("\r\n");
               bw.flush();
           }
       } catch (IOException e) {
           LOG.error(e);
           throw new RuntimeException(e);
       } finally {            
           try {
               if(bw != null) {
                  bw.close();
               }
               if(is != null) {
            	  is.close();
               }
           } catch (IOException e) {
               LOG.error(e);
               throw new RuntimeException(e);
           }
       }        
   }
   
   public static void writeFile(String content, String filePath) {	   
	   FileOutputStream fop = null;
	   File file = null ;
	   try {
	       file = new File(filePath);
	       String parentPath = file.getParent();
	       File parentfile = new File(parentPath);
	       if (!parentfile.exists()) {
	    	       parentfile.mkdirs();
	       }
	       fop = new FileOutputStream(file,true);
	       byte[] contentInBytes = content.getBytes();
	       fop.write(contentInBytes);
	   } catch (IOException e) {
		   LOG.error(e);
	   } finally {
	        try {
	            if (fop != null) {
	                fop.close();
	            }
	        } catch (IOException e) {
         	   LOG.error(e);
	        }
	   }	  
   }
   
   public static String readFile(String fileName) {  
	   StringBuilder sb = new StringBuilder();
       File file = new File(fileName);
       if(file.exists()) {
           BufferedReader reader = null;  
           try {  
               InputStream in = new FileInputStream(new File(fileName));
               //使用BoundedInputStream防止内存溢出
              //限制流的大小为100M,根据实际文件大小可调整
               BoundedInputStream bis = new BoundedInputStream(in, 100 * 1024 * 1024);
               InputStreamReader isr = new InputStreamReader(bis);
               reader = new BufferedReader(isr);
               String tempString = null;  
               while ((tempString = reader.readLine()) != null) {  
            	       sb.append(tempString);
               }  
           } catch (IOException e) {  
        	   LOG.error(e);
           } finally {  
               if (reader != null) {  
                   try {  
                       reader.close();  
                   } catch (IOException e) {  
                	     LOG.error(e);
                   }  
               }  
           }  
       }else {
    	       LOG.warn("file " + fileName + " does not exists");
       }
       return sb.toString();
   } 
   
   /**
    * 读log文件获取applicationId
    */
   public static List<String> getApplicationIdFromLog(String fileName)
       throws Exception {
       BufferedReader br = null;
       Set<String> applicationList = new HashSet<>();
       try {
           InputStream in = new FileInputStream(new File(fileName));
           //使用BoundedInputStream防止内存溢出
          //限制流的大小为100M,根据实际文件大小可调整
           BoundedInputStream bis = new BoundedInputStream(in, 100 * 1024 * 1024);
           InputStreamReader reader = new InputStreamReader(bis);
           br = new BufferedReader(reader);
           String tempString = null;
           String tag = "Submitted application ";
           while ((tempString = br.readLine()) != null) {
              if (tempString.contains(tag)) {
                   String applicationId = tempString
                   .substring(tempString.indexOf(tag) + tag.length());
              if (applicationId.length() > 0) {
                   applicationList.add(applicationId);
           }
         }
       }
          reader.close();
      } finally {
       if (br != null) {
           br.close();
       }
     }
     return new ArrayList<String>(applicationList);
   }
   
   /**
    *  processing log
    *  get yarn application id list
    * @param log
    * @param logger
    * @return
    */
   public static List<String> getSparkAppIds(String fileName) throws Exception{
       Set<String> appIds = new HashSet<String>();
       File file = new File(fileName);
       BufferedReader reader = null;
       try {
           reader = new BufferedReader(new FileReader(file));
           String tempString = null;
           while ((tempString = reader.readLine()) != null) {
               Matcher matcher = APPLICATION_REGEX.matcher(tempString);
               // analyse logs to get all submit yarn application id
               while (matcher.find()) {
                   String appId = matcher.group();
                   LOG.info("find app id:" + appId);
                   appIds.add(appId);
               }
          }
           reader.close();
      } finally {
       if (reader != null) {
           reader.close();
       }
     }
       return new ArrayList<String>(appIds);
   }
   
   public static void removeFile(String fileName) { 
	   File file = new File(fileName);
       if(exist(file)) {
    	      file.delete();
       }
   } 
   
   public static boolean exist(File file) {  
       if(file.exists()) {
          return true;    	   
       }
       return false;
   }

   public static void main(String[] args) {
       try {
        System.out.println(readFile("/tmp/log/worker/36-322-20190930102000.out"));
    } catch (Exception e) {
        e.printStackTrace();
    }
   }
}