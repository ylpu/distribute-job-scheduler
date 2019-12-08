package com.ylpu.thales.scheduler.common.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
}
