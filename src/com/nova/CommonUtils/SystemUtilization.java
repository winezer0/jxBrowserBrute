package com.nova.CommonUtils;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class SystemUtilization {

    public static double getCpuUsage() {
        //获取CPU利用率不正常 https://blog.csdn.net/qq_41866138/article/details/105386832 oshi方法也不正常
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemCpuLoad() * 100.0;
    }

    public static double getMemoryUsage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        long freeMemory = osBean.getFreePhysicalMemorySize();
        long usedMemory = totalMemory - freeMemory;
        return (usedMemory * 100.0) / totalMemory;
    }

    public static boolean isSystemOverloaded() {
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();

        if (cpuUsage > 90 || memoryUsage > 90) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();

        System.out.println("CPU Usage: " + cpuUsage + "%");
        System.out.println("Memory Usage: " + memoryUsage + "%");

        if (isSystemOverloaded()) {
            System.out.println("警告：系统负载过高，CPU 或内存利用率超过 90%！");
        } else {
            System.out.println("系统运行正常。");
        }
    }
}