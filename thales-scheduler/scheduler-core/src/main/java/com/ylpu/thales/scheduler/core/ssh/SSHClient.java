package com.ylpu.thales.scheduler.core.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHClient {

    private static Log LOG = LogFactory.getLog(SSHClient.class);

    Map<String, Connection> connectionMap = new HashMap<String, Connection>();

    private static SSHClient sshClient = new SSHClient();

    private SSHClient() {

    }

    public static SSHClient getInstance() {
        return sshClient;
    }

    public synchronized Connection getConnection(String hostname, String username, String password) throws Exception {
        Connection conn = null;
        LOG.info("start to connect to host " + hostname);
        try {
            if (connectionMap.get(hostname) == null) {
                conn = new Connection(hostname);
                conn.connect();
                boolean isAuthenticated = conn.authenticateWithPassword(username, password);
                if (isAuthenticated == false) {
                    throw new Exception("failed to get ssh connection from server ");
                }
                connectionMap.put(hostname, conn);
            }

        } catch (Exception e) {
            LOG.error(e);
        }
        return connectionMap.get(hostname);
    }

    public SSHClientOutput execCommandAndReturnOutput(Connection conn, String command) throws Exception {
        SSHClientOutput output = new SSHClientOutput();
        Session session = null;
        try {
            session = conn.openSession();
            session.execCommand(command);
        } catch (IllegalStateException ex) {

        } catch (IOException ex) {

        }

        int timeout = 60;
        while (session.getExitStatus() == null) {
            timeout--;
            Thread.sleep(1000);
            if (timeout < 0)
                break;
        }
        InputStream stdout = new StreamGobbler(session.getStdout());
        InputStream stderr = new StreamGobbler(session.getStderr());

        output.setStdErr(stderr);
        output.setStdOut(stdout);
        session.waitForCondition(0, 0);
        session.close();
        return output;
    }

    public int execCommand(Connection conn, String command, boolean sourceProfile) throws Exception {

        if (sourceProfile) {
            command = "source ./.bashrc;source /etc/profile;" + command;
        }
        Session session = null;
        try {
            session = conn.openSession();
            session.execCommand(command);
        } catch (IllegalStateException ex) {
        } catch (IOException ex) {
        }

        int timeout = 360;
        while (session.getExitStatus() == null) {
            timeout--;
            Thread.sleep(1000);
            if (timeout < 0)
                break;
        }

        int retVal = session.getExitStatus().intValue();

        InputStream stdout = new StreamGobbler(session.getStdout());
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
        while (true) {
            String line = stdoutReader.readLine();
            if (line == null)
                break;
            LOG.info(line);
        }
        stdoutReader.close();

        InputStream stderr = new StreamGobbler(session.getStderr());
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
        while (true) {
            String line = stderrReader.readLine();
            if (line == null)
                break;
            LOG.error(line);
        }
        stderrReader.close();
        session.close();
        return retVal;
    }

    public int execCommand(Connection conn, String command) throws Exception {
        return this.execCommand(conn, command, false);
    }

    public void closeConnection() {

        if (connectionMap != null) {
            for (Entry<String, Connection> entry : connectionMap.entrySet()) {
                String server = entry.getKey();
                Connection conn = entry.getValue();
                if (conn != null) {
                    LOG.info("logout the server " + server);
                    conn.close();
                }
            }
        }
    }
}
