package org.succlz123.naix.lib;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Arrays;

public class NaixPrinter {
    private static final int MAX_LENGTH = 90;

    private String className;
    private String methodName;
    private StringBuilder result = new StringBuilder();
    private ArrayList<Long> methodTimes = new ArrayList<>();
    private ArrayList<String> methodNames = new ArrayList<>();
    private String returnStr;

    public NaixPrinter(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;

        result.append("Class#Method -> ");
        result.append(className);
        result.append("#").append(methodName).append("\nParameters ->");

        long now = SystemClock.elapsedRealtime();
        methodTimes.add(now);
        methodNames.add(methodName);
    }

    public void addParameter(String name, int value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, boolean value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, short value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, byte value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, char value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, long value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, double value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, float value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        result.append(value);
    }

    public void addParameter(String name, Object value) {
        result.append(" ");
        result.append(name);
        result.append(" = ");
        if (value != null && value.getClass().isArray()) {
            result.append(arrayToString(value));
        } else {
            result.append(value);
        }
    }

    public void addReturn(byte value) {
        returnStr = value + "";
    }

    public void addReturn(char value) {
        returnStr = value + "";
    }

    public void addReturn(short value) {
        returnStr = value + "";
    }

    public void addReturn(int value) {
        returnStr = value + "";
    }

    public void addReturn(boolean value) {
        returnStr = value + "";
    }

    public void addReturn(long value) {
        returnStr = value + "";
    }

    public void addReturn(float value) {
        returnStr = value + "";
    }

    public void addReturn(double value) {
        returnStr = value + "";
    }

    public void addReturn(Object value) {
        if (value != null && value.getClass().isArray()) {
            returnStr = arrayToString(value);
        } else {
            returnStr = value + "";
        }
    }

    public void addCallMethod(String name) {
        long now = SystemClock.elapsedRealtime();
        methodTimes.add(now);
        methodNames.add(name);
    }

    public void print() {
        if (NaixOption.disabled) {
            return;
        }
        result.append("\n");
        final long first = methodTimes.get(0);
        long now = first;
        for (int i = 1; i < methodTimes.size(); i++) {
            now = methodTimes.get(i);
            final long prev = methodTimes.get(i - 1);
            result.append(i);
            result.append(" -> ");
            result.append(now - prev);
            result.append(" ms, ");
            result.append(methodNames.get(i));
            result.append("\n");
        }
        result.append("Return value -> ");
        result.append(returnStr);
        result.append("\n");
        result.append("Elapsed time -> ");
        result.append(now - first);
        result.append(" ms");
        printLog(result.toString());
    }

    private static String arrayToString(Object val) {
        if (!(val instanceof Object[])) {
            if (val instanceof int[]) {
                return Arrays.toString((int[]) val);
            } else if (val instanceof char[]) {
                return Arrays.toString((char[]) val);
            } else if (val instanceof boolean[]) {
                return Arrays.toString((boolean[]) val);
            } else if (val instanceof byte[]) {
                return Arrays.toString((byte[]) val);
            } else if (val instanceof long[]) {
                return Arrays.toString((long[]) val);
            } else if (val instanceof double[]) {
                return Arrays.toString((double[]) val);
            } else if (val instanceof float[]) {
                return Arrays.toString((float[]) val);
            } else if (val instanceof short[]) {
                return Arrays.toString((short[]) val);
            } else {
                return "Unknown type array";
            }
        } else {
            return Arrays.deepToString((Object[]) val);
        }
    }

    protected void printLog(String message) {
        String[] msgs = message.split("\n");
        printLine(true);
        for (String msg : msgs) {
            int index = 0;
            int msgLen = msg.length();
            int countOfSub = msgLen / MAX_LENGTH;
            if (countOfSub > 0) {
                for (int j = 0; j < countOfSub; j++) {
                    int len = index + MAX_LENGTH;
                    String sub = msg.substring(index, Math.min(len, msgLen));
                    NaixOption.LOGGER.log(className, methodName, "║ " + sub + " ║");
                    index += MAX_LENGTH;
                }
                String endStr = msg.substring(index);
                int countNewSub = MAX_LENGTH - endStr.length();
                StringBuilder sb = new StringBuilder();
                sb.append("║ ");
                sb.append(endStr);
                for (int i = 0; i < countNewSub; i++) {
                    sb.append(" ");
                }
                sb.append(" ║");
                NaixOption.LOGGER.log(className, methodName, sb.toString());
            } else {
                int countNewSub = MAX_LENGTH - msgLen;
                StringBuilder sb = new StringBuilder();
                sb.append("║ ");
                sb.append(msg);
                for (int i = 0; i < countNewSub; i++) {
                    sb.append(" ");
                }
                sb.append(" ║");
                NaixOption.LOGGER.log(className, methodName, sb.toString());
            }
        }
        printLine(false);
    }

    protected void printLine(boolean isTop) {
        if (isTop) {
            NaixOption.LOGGER.log(className, methodName, "╔════════════════════════════════════════════════════════════════════════════════════════════╗");
        } else {
            NaixOption.LOGGER.log(className, methodName, "╚════════════════════════════════════════════════════════════════════════════════════════════╝");
        }
    }
}