package org.succlz123.naix.lib;

import android.util.Log;

public class NaixOption {
    static boolean disabled = false;

    public static void set(Logger logger) {
        LOGGER = logger;
    }

    public static void enable() {
        disabled = true;
    }

    public static void disable() {
        disabled = false;
    }

    protected void log(String className, String methodName, String msg) {
        LOGGER.log(className, methodName, msg);
    }

    protected static Logger LOGGER = (className, methodName, msg) -> Log.i("Naix 👉", msg);

    public interface Logger {

        void log(String className, String methodName, String msg);
    }
}