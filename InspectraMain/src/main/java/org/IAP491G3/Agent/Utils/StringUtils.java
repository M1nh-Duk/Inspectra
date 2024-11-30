package org.IAP491G3.Agent.Utils;


import java.util.Arrays;
import java.util.Map;

import static org.IAP491G3.Agent.Loader.Contraints.*;
import static org.IAP491G3.Agent.Utils.LogUtils.getEventTime;
import static org.IAP491G3.Agent.Utils.LogUtils.logit;

public class StringUtils {

    public static String toLowerCase(String str) {
        return str.toLowerCase();
    }

    public static void printUsage() {
        System.out.println(AGENT_NAME + " (Java Agent)");
        System.out.println("Usage: java -jar " + AGENT_LOADER_FILE_NAME + " [Options]");
        System.out.println("  1) detach [Java PID]");
        System.out.println("  2) attach [Java PID]");
        System.out.println("\r\n");
        System.out.println("EXAMPLES :");
        System.out.println("  java -jar " + AGENT_LOADER_FILE_NAME + " attach 10001");
        System.out.println("  java -jar " + AGENT_LOADER_FILE_NAME + " detach 10001");
        System.out.println("\r\n");
        System.out.println("JVM PID List:");
    }

    public static String[] convertToStringArray(String str) {
        return str.replaceAll("[\\[\\]]", "").split(",\\s*");
    }

    public static void printProcessList(Map<String, String> processMap) {
        for (Map.Entry<String, String> entry : processMap.entrySet()) {
            System.out.println("PID: " + entry.getKey() + " - Process Name: " + entry.getValue());
        }
    }

    public static void printLogo() {
        String banner = getBanner();
        System.out.println("\n" + banner + "\n[ " + AGENT_NAME + " v1.0.0 ] by ka1t0_k1d\n");
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String getOutputPath(String className, String folder) {
        className = className.substring(className.lastIndexOf(".") + 1);
        return  folder + FILE_SEPERATOR + className.replace('.', '_') + ".class";
    }
    public static String[] addStringToStringArray(String[] originalArray, String newElement) {
        if (originalArray == null) {
            // If the original array is null, create a new array with one element
            return new String[]{newElement};
        }

        // Create a new array with one more slot than the original array
        String[] newArray = new String[originalArray.length + 1];

        // Copy elements from the original array to the new array
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);

        // Add the new element at the last position
        newArray[originalArray.length] = newElement;

        return newArray;
    }

    public static String getBanner() {
        return "\n" +
                "  _____        _____                 _             \n" +
                " |_   _|      / ____|               | |            \n" +
                "   | |  _ __ | (___  _ __   ___  ___| |_ _ __ __ _ \n" +
                "   | | | '_ \\ \\___ \\| '_ \\ / _ \\/ __| __| '__/ _` |\n" +
                "  _| |_| | | |____) | |_) |  __/ (__| |_| | | (_| |\n" +
                " |_____|_| |_|_____/| .__/ \\___|\\___|\\__|_|  \\__,_|\n" +
                "                    | |                            \n" +
                "                    |_|                            \n";
    }


    public static void println(String str) {
        if (!OPTION_SILENT) {
            System.out.println("[" + AGENT_NAME + "]" + "[" + getEventTime() + "] " + str);
        }
    }
    public static void printErr(String str) {
        if (!OPTION_SILENT) {
            System.err.println("[" + AGENT_NAME + "]" + "[" + getEventTime() + "] " + str);
        }
    }
    public static void printAndLog(String str) {
        println(str);
        logit(str);
    }
    public static void printAndLogErr(Exception e ) {
        if (!OPTION_SILENT){
            printErr("Exception occured: " + e.getMessage()+ "\nPlease find the error in log file for more information");
        }
        // Building a readable stack trace as a String
        StringBuilder stackTraceBuilder = new StringBuilder();
        Arrays.stream(e.getStackTrace()).forEach(element -> {
            stackTraceBuilder.append(element.toString()).append("\n");
        });
        logit("Exception occured: " + e.getMessage());
        logit("Stacktrace:\n" + stackTraceBuilder.toString());
    }

}
