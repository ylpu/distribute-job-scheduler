package com.ylpu.thales.scheduler.core.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SSHClientOutput {

    private static Log LOG = LogFactory.getLog(SSHClientOutput.class);

    private InputStream stdOut;
    private InputStream stdErr;

    public InputStream getStdOut() {
        return stdOut;
    }

    public void setStdOut(InputStream stdOut) {
        this.stdOut = stdOut;
    }

    public InputStream getStdErr() {
        return stdErr;
    }

    public void setStdErr(InputStream stdErr) {
        this.stdErr = stdErr;
    }

    public List<String> getStdOutAsList() {
        List<String> list = null;

        try {
            list = getOutPutAsList(this.stdOut);
        } catch (IOException e) {
            LOG.error("failed to get shell output as list", e);
        }
        return list;
    }

    public List<String> getStdErrAsText() {
        List<String> list = null;
        try {
            list = getOutPutAsList(this.stdErr);
        } catch (IOException e) {
            LOG.error("failed to get shell error output as list", e);
        }
        return list;
    }

    public String getStdOutAsString() {
        String str = null;
        try {
            str = IOUtils.toString(this.stdOut);
        } catch (IOException e) {
            LOG.error("failed to get shell output as text", e);
        }
        return str;
    }

    private ArrayList<String> getOutPutAsList(InputStream is) throws IOException {

        ArrayList<String> output = new ArrayList<String>(10000);
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(is), 1024);
        String line = null;

        while ((line = stderrReader.readLine()) != null) {
            output.add(line);
        }
        return output;
    }

}
