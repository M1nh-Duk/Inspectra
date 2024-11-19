package org.IAP491G3.Agent.Utils;


import java.util.Map;

import static org.IAP491G3.Agent.Loader.Contraints.AGENT_LOADER_FILE_NAME;
import static org.IAP491G3.Agent.Loader.Contraints.AGENT_NAME;

public class StringUtils {
    public static String toLowerCase(String str) {
        return str.toLowerCase();
    }
    public static void println(String str) {
        System.out.println("[ " + AGENT_NAME + " ] " + str);
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

    public static String extractClassName(String className) {
        int startIndex = className.indexOf("_") + 1; // Find the index of the first underscore and add 1
        int endIndex = className.lastIndexOf("_"); // Find the last underscore
        return  className.substring(startIndex, endIndex);
    }
}
