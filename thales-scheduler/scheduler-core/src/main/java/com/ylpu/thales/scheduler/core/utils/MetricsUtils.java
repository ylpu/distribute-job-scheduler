package com.ylpu.thales.scheduler.core.utils;

import oshi.json.SystemInfo;
import oshi.json.hardware.CentralProcessor;
import oshi.json.hardware.GlobalMemory;
import oshi.json.hardware.HardwareAbstractionLayer;

import java.math.BigDecimal;
import java.net.InetAddress;

public class MetricsUtils {
    
    private static SystemInfo systemInfo = new SystemInfo();

    /**
     * 获得内存信息
     * @return 内存信息
     */
    public static GlobalMemory getMemory() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        return hal.getMemory();
    }

    /**
     * 获得处理器(cpu)信息
     */
    public static CentralProcessor getProcessor() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        return hal.getProcessor();
    }

    /**
     * 获得内存总量
     * @param memory GlobalMemory对象
     * @return 内存总量
     */
    public static double getMemoryTotal(GlobalMemory memory) {
        return memory.getTotal();
    }
    
    /**
     * 获得内存使用量
     * @param memory GlobalMemory对象
     * @return 内存使用量
     */
    public static double getMemoryAvailable(GlobalMemory memory) {
        return memory.getAvailable();
    }
    
    /**
     * 获取内存的使用率
     * @param memory GlobalMemory对象
     */
    public static double getMemoryUsage(GlobalMemory memory) {
        double available = getMemoryAvailable(memory);
        double total = getMemoryTotal(memory);
        BigDecimal bg = new BigDecimal(available/total * 100);
        return 100 - bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    
    /**
     * 获取内存的使用率
     * @return 内存使用率
     */
    public static double getMemoryUsage() {
        return getMemoryUsage(getMemory());
    }

    /**
     * 获取CPU的使用率
     */
    public static double getCpuUsage() {
        CentralProcessor processor = getProcessor();
        double useRate = processor.getSystemCpuLoadBetweenTicks();
        BigDecimal bg = new BigDecimal(useRate * 100);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    
    /**
     * 获取主机名称
     * @return
     */
    public static String getHostName() {
        InetAddress addr;
        String hostName;
        try{
            addr = InetAddress.getLocalHost();
            hostName = addr.getHostName(); //获得机器名称
        }catch(Exception e){  
            throw new RuntimeException("can not find host name"); 
        }  
        return "localhost";
    }
    
    /**
     * 获取主机ip地址
     * @return
     */
    public static String getHostIpAddress() {
        InetAddress addr;
        String ip;
        try{
            addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress(); //获得机器IP　　
        }catch(Exception e){  
            throw new RuntimeException("can not find ip address");  
        } 
        return "127.0.0.1";
    }
}