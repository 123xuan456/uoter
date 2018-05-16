package com.reeching.uoter.utils;

import android.util.Log;

/**
 * ******************************************
 * ******************************************
 */
public class LogUtils {
    static String className;//类名
    static String methodName;//方法名
    static int lineNumber;//行数

    static boolean flag1 = false;
    private static String createLog( String log ) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName);
        buffer.append("(").append(className).append(":").append(lineNumber).append(")");
        buffer.append("="+log);
        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements){
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }
    public static void i(String message) {
        getMethodNames(new Throwable().getStackTrace());
        Log.i(className, createLog(message));
    }
    public static void i(int message){
        if (flag1)
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.i(className, createLog(message+""));
    }
    public static void d(String message) {
        getMethodNames(new Throwable().getStackTrace());
        Log.d(className, createLog(message));
    }
    public static void e(String message) {
        getMethodNames(new Throwable().getStackTrace());
        Log.e(className, createLog(message));
    }


}
