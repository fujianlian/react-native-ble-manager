//
//  BleLog.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.util.Log;

public class BleLog {

    static final String TAG = "BleNativeWrapper";
    private static final boolean OUTPUT_LOG_MODE = true;
    private static final LogLevel OUTPUT_LOG_LEVEL = LogLevel.Debug;
    private static final String LOG_PREFIX_DEBUG = "[DEBUG] ";
    private static final String LOG_PREFIX_INFO = "[INFO]  ";
    private static final String LOG_PREFIX_WARN = "[WARN]  ";
    private static final String LOG_PREFIX_ERROR = "[ERROR] ";
    private static final String LOG_PREFIX_METHOD_IN = "[IN]    ";
    private static final String LOG_PREFIX_METHOD_OUT = "[OUT]   ";

    public static void d(String msg) {
        outputLog(LogLevel.Debug, LOG_PREFIX_DEBUG + methodNameString(true) + ' ' + msg);
    }

    public static void i(String msg) {
        outputLog(LogLevel.Info, LOG_PREFIX_INFO + methodNameString(true) + ' ' + msg);
    }

    public static void w(String msg) {
        outputLog(LogLevel.Warn, LOG_PREFIX_WARN + methodNameString(true) + ' ' + msg);
    }

    public static void e(String msg) {
        outputLog(LogLevel.Error, LOG_PREFIX_ERROR + methodNameString(true) + ' ' + msg);
    }

    public static void dMethodIn() {
        outputLog(LogLevel.Debug, LOG_PREFIX_METHOD_IN + methodNameString(false));
    }

    public static void dMethodIn(String msg) {
        outputLog(LogLevel.Debug, LOG_PREFIX_METHOD_IN + methodNameString(false) + " " + msg);
    }

    public static void dMethodOut() {
        outputLog(LogLevel.Debug, LOG_PREFIX_METHOD_OUT + methodNameString(false));
    }

    public static void dMethodOut(String msg) {
        outputLog(LogLevel.Debug, LOG_PREFIX_METHOD_OUT + methodNameString(false) + " " + msg);
    }

    public static void outputLog(LogLevel level, String msg) {
        if (!OUTPUT_LOG_MODE) {
            return;
        }
        if (OUTPUT_LOG_LEVEL.ordinal() > level.ordinal()) {
            return;
        }
        switch (level) {
            case Debug:
                Log.d(TAG, msg);
                break;
            case Info:
                Log.i(TAG, msg);
                break;
            case Warn:
                Log.w(TAG, msg);
                break;
            case Error:
                Log.e(TAG, msg);
                break;
            default:
                Log.v(TAG, msg);
                break;
        }
    }

    private static String methodNameString(boolean addLineNumber) {
        final StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        final String fullClassName = element.getClassName();
        final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        final String methodName = element.getMethodName();
        final int lineNumber = element.getLineNumber();
        final StringBuilder sb = new StringBuilder();
        sb.append(simpleClassName).append("#").append(methodName);
        if (addLineNumber) {
            sb.append(":").append(lineNumber);
        }
        return sb.toString();
    }

    public enum LogLevel {
        Verbose, Debug, Info, Warn, Error
    }
}
