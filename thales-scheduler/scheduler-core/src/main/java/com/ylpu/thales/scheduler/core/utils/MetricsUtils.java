package com.ylpu.thales.scheduler.core.utils;

import oshi.json.SystemInfo;
import oshi.json.hardware.CentralProcessor;
import oshi.json.hardware.GlobalMemory;
import oshi.json.hardware.HardwareAbstractionLayer;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.ServerSocket;

public class MetricsUtils {

    private static SystemInfo systemInfo = new SystemInfo();

    /**
     * get memrory
     * 
     */
    public static GlobalMemory getMemory() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        return hal.getMemory();
    }

    /**
     * get cpu
     */
    public static CentralProcessor getProcessor() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        return hal.getProcessor();
    }

    /**
     * get total memory
     * 
     * @param memory
     */
    public static double getMemoryTotal(GlobalMemory memory) {
        return memory.getTotal();
    }

    /**
     * get availabe memory
     * 
     * @param memory
     */
    public static double getMemoryAvailable(GlobalMemory memory) {
        return memory.getAvailable();
    }

    /**
     * get memory usage
     * 
     * @param memory
     */
    public static double getMemoryUsage(GlobalMemory memory) {
        double available = getMemoryAvailable(memory);
        double total = getMemoryTotal(memory);
        BigDecimal bg = new BigDecimal(available / total * 100);
        return 100 - bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * get memory usage
     * 
     */
    public static double getMemoryUsage() {
        return getMemoryUsage(getMemory());
    }

    /**
     * get cpu usage
     */
    public static double getCpuUsage() {
        CentralProcessor processor = getProcessor();
        double useRate = processor.getSystemCpuLoadBetweenTicks();
        BigDecimal bg = new BigDecimal(useRate * 100);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * get host name
     * 
     * @return
     */
    public static String getHostName() {
        InetAddress addr;
        String hostName;
        try {
            addr = InetAddress.getLocalHost();
            hostName = addr.getHostName();
        } catch (Exception e) {
            throw new RuntimeException("can not find host name");
        }
        return hostName;
    }

    /**
     * get ip address
     * 
     * @return
     */
//    public static String getHostIpAddress() {
//        InetAddress addr;
//        String ip;
//        try {
//            addr = InetAddress.getLocalHost();
//            ip = addr.getHostAddress();
//        } catch (Exception e) {
//            throw new RuntimeException("can not find ip address");
//        }
//        return ip;
//    }
    
    public static int getAvailablePort(int minPort) {
        int port;
        try{
            while (true){
                ServerSocket serverSocket =  new ServerSocket(minPort);
                port = serverSocket.getLocalPort();
                serverSocket.close();
                break;
            }
        }catch (Exception e){
            minPort++;
            port = getAvailablePort(minPort);
        }
        return port;
    }
}