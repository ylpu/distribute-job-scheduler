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

/**
 * 脚本执行类 优点：使用apache帮助类，简单，方便 缺点：无法获取process类，因而无法得到执行任务的进程id
 */

class CollectingLogOutputStream extends LogOutputStream {

    private final StringBuilder lines = new StringBuilder();

    @Override
    protected void processLine(String line, int level) {
        lines.append(line);
        lines.append("<br>");
    }

    public String getLines() {
        return lines.toString();
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
     * 日志文件输出方式
     * <p>
     * 优点：支持将目标数据实时输出到指定日志文件中去
     * 
     * @param command
     * @param scriptFile
     * @param logFile
     * @param params
     * @return
     * @throws IOException
     */
    public static int execToFile(String command, String scriptFile, String outLogFile, String errLogFile,
            String... params) throws IOException {
        FileOutputStream fileOutputStream = null;
        FileOutputStream fileErrorOputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outLogFile, true);
            fileErrorOputStream = new FileOutputStream(outLogFile, true);
            PumpStreamHandler streamHandler = new PumpStreamHandler(fileOutputStream, fileErrorOputStream, null);
            int exitValue = execCmd(command, scriptFile, params, streamHandler);
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
     * 实时处理每一行输出
     * 
     * @param command
     * @param scriptFile
     * @param params
     * @return
     * @throws IOException
     */
    public static int execCmd(String command, String scriptFile, String... params) throws IOException {
        PumpStreamHandler streamHandler = new PumpStreamHandler(new CollectingLogOutputStream());
        // command
        return execCmd(command, scriptFile, params, streamHandler);
    }

    public static int execCmd(String command, String scriptFile, String[] params, PumpStreamHandler streamHandler)
            throws IOException {
        CommandLine commandline = new CommandLine(command);
        if (scriptFile != null && scriptFile.length() > 0) {
            commandline.addArgument(scriptFile);
        }
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
     * 把输出写入流中
     * 
     * @param command
     * @param scriptFile
     * @param params
     * @return
     * @throws IOException
     */
    public static String execToList(String command, String scriptFile, String... params) throws IOException {
        CollectingLogOutputStream outputStream = new CollectingLogOutputStream();
        try {
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream);
            execCmd(command, scriptFile, params, streamHandler);
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

    public static String execToString(String command, String scriptFile, String... params) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream);
            execCmd(command, scriptFile, params, streamHandler);
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
            System.out.println(ScriptUtils.execToList("cat", "/tmp/log/scheduler-worker/warn.log", new String[] {}));
            // System.out.println("test");

            String[] strs = new String[3];
            strs[0] = "/bin/bash";
            strs[1] = "-c";
            strs[2] = "tail -100 /tmp/log/scheduler-worker/warn.log";
            Process process = Runtime.getRuntime().exec(strs);

            FileUtils.writeOuput(process.getInputStream(), "/tmp/test.log");
            FileUtils.writeOuput(process.getErrorStream(), "/tmp/test.log");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
