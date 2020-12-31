package com.ylpu.thales.scheduler.common.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class StreamUtils {

    public static InputStream getInputStream(String logUrl) throws Exception {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        URL url = new URL(logUrl);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(3000);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        inputStream = httpURLConnection.getInputStream();
        return inputStream;
    }

    public static void writeOutput(InputStream inputStream, ServletOutputStream outputStream,
            HttpServletResponse response, String logUrl) throws Exception {
        inputStream = getInputStream(logUrl);
        if (inputStream != null) {
            outputStream = response.getOutputStream();
            byte[] cache = new byte[8092];
            int nRead = 0;
            while ((nRead = inputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, nRead);
                outputStream.flush();
            }
            outputStream.flush();
        }
    }
}
