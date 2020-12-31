package com.ylpu.thales.scheduler.core.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CollectingLogOutputStream extends LogOutputStream {

    private final List<String> lines = new ArrayList<String>();

    @Override
    protected void processLine(String line, int level) {
        lines.add(line);
//        lines.append("<br>");
    }

    public List<String> getLines() {
        return lines;
    }
}

public class ScriptUtils {

    private static Log LOG = LogFactory.getLog(ScriptUtils.class);

    /**
     * make script file
     * 
     * @param scriptFileName
     * @param content
     * @throws IOException
     */
    public static void markScriptFile(String scriptFileName, String content) throws Exception {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(scriptFileName);
            fileOutputStream.write(content.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (Exception e) {
            LOG.error(e);
            throw e;
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    /**
     * execute command and write output to file
     * 
     * @param command
     * @param scriptFile
     * @param logFile
     * @param params
     * @return
     * @throws IOException
     */
    public static int execToFile(String command, String outLogFile, String errLogFile,
            String... params) throws IOException {
        FileOutputStream fileOutputStream = null;
        FileOutputStream fileErrorOputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outLogFile, true);
            fileErrorOputStream = new FileOutputStream(outLogFile, true);
            PumpStreamHandler streamHandler = new PumpStreamHandler(fileOutputStream, fileErrorOputStream, null);
            int exitValue = execCmd(command, params, streamHandler);
            return exitValue;
        } catch (Exception e) {
            LOG.error(e);
            return -1;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * execute command and return execute code
     * 
     * @param command
     * @param scriptFile
     * @param params
     * @return
     * @throws IOException
     */
    public static int execCmd(String command, String... params) throws IOException {
        PumpStreamHandler streamHandler = new PumpStreamHandler(new CollectingLogOutputStream());
        // command
        return execCmd(command, params, streamHandler);
    }

    public static int execCmd(String command, String[] params, PumpStreamHandler streamHandler)
            throws IOException {
        CommandLine commandline = CommandLine.parse(command);
        if (params != null && params.length > 0) {
            commandline.addArguments(params);
        }
        // execCmd
        DefaultExecutor exec = new DefaultExecutor();
        exec.setExitValues(null);
        exec.setStreamHandler(streamHandler);
        int exitValue = exec.execute(commandline);// exit code: 0=success, 1=error
        return exitValue;
    }

    /**
     * write stream to list
     * 
     * @param command
     * @param scriptFile
     * @param params
     * @return
     * @throws IOException
     */
    public static List<String> execToList(String command,String... params) throws IOException {
        CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
        try {
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream);
            execCmd(command,params, streamHandler);
        } catch (Exception e) {
            LOG.error(e);
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }

            }
        }
        return outputStream.getLines();
    }

    public static String execToString(String command, String... params) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream);
            execCmd(command, params, streamHandler);
        } catch (Exception e) {
            LOG.error(e);
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }

            }
        }
        return outputStream.toString();
    }

    public static void main(String[] args) {
        try {
            // System.out.println(ScriptUtils.execToFile("sh", "/tmp/script/test.sh",
            // "/tmp/log/1.out", "/tmp/log/1.error", new String[1]));
            List<String> list = ScriptUtils.execToList("head -100 /tmp/log/autoel-api/spring.log | tail -10", new String[] {});
            for(String str : list) {
                System.out.println(str);
            }
            System.out.println(list.size());
            // System.out.println("test");

//            String[] strs = new String[3];
//            strs[0] = "/bin/bash";
//            strs[1] = "-c";
//            strs[2] = "tail -100 /tmp/log/scheduler-worker/warn.log";
//            Process process = Runtime.getRuntime().exec(strs);
//
//            FileUtils.writeOuput(process.getInputStream(), "/tmp/test.log");
//            FileUtils.writeOuput(process.getErrorStream(), "/tmp/test.log");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
