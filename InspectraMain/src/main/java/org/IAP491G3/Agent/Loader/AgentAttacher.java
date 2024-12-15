package org.IAP491G3.Agent.Loader;

import org.IAP491G3.Agent.Utils.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static org.IAP491G3.Agent.Loader.Contraints.*;
import static org.IAP491G3.Agent.Utils.PathUtils.getCurrentJarPath;
import static org.IAP491G3.Agent.Utils.StringUtils.*;


public class AgentAttacher {

    private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("^1\\.[0-7]");
    private static String WHITELIST_CLASSES;


    public static void attachJvm(String processId, String args, VMProxy vmLoader) {
        try {
            int pid = Integer.parseInt(processId.trim());

            Object vm = vmLoader.attach(processId);

            vmLoader.loadAgent(vm, getCurrentJarPath(), args);
            vmLoader.detach(vm);
        }
        catch (NumberFormatException e) {
            // Handle the case where processId is not a valid integer
            System.err.println("Invalid process ID: " + processId + ". Please provide a valid integer.");
        }catch (Exception e) {
            System.err.println(e.getCause());
        }
    }

    public static void main(String[] args) throws Exception {
        VMProxy vmProxy = new VMProxy();
        if (args.length == 0) {
            printUsage();
            return;
        }
        String javaVersion = System.getProperty("java.version");
        if (Pattern.matches(String.valueOf(JAVA_VERSION_PATTERN), javaVersion)) {
            System.err.println("JDK Version: " + javaVersion + ". JDK Version Can Not Less Than 1.8!");
            return;
        }
        if (!loadConfiguration()) {
            StringUtils.printErr("Cannot load configuration!");
            StringUtils.printErr("Please config first!");
            doConfig();
            return;
        }
        if ("attach".equalsIgnoreCase(args[0]) || "detach".equalsIgnoreCase(args[0])) {
            if (UPLOAD_FOLDER == null) {
                StringUtils.printErr("UPLOAD_FOLDER is null. Please config before proceeding.");
                return;
            }

            args = addStringToStringArray(args, WHITELIST_CLASSES);
            args = addStringToStringArray(args, UPLOAD_FOLDER);

            attachJvm(args[1].trim(), Arrays.toString(args), vmProxy);
        } else if ("list".equalsIgnoreCase(args[0])) {
            printProcessList(vmProxy.listJvmPid());
        } else if ("config".equalsIgnoreCase(args[0])) {
            doConfig();
        } else {
            printUsage();
        }
    }


    private static String autoLoadingJVM(VMProxy loader) {
        try {
            Map<String, String> processMap = loader.listJvmPid();

            for (String processId : processMap.keySet()) {
                String name = processMap.get(processId).toLowerCase();
                System.out.println("PID:" + processId + "\tProcessName:" + ("".equals(name) ? "NONE" : name));
                if (name.contains("bootstrap")) {
                    System.out.println("TOMCAT PID:" + processId);
                    return processId;
                }
            }
        } catch (Exception e) {
            System.err.println("Load JVM PID Exception:" + e);
            e.printStackTrace();
        }
        return "";
    }


    private static boolean loadConfiguration() {
        Properties properties = new Properties();
//        CONFIG_FILE = getCurrentDirectory() + FILE_SEPERATOR + "config.properties";
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            try {
                if (configFile.createNewFile()) {
                    System.out.println("Config file created. Please use option \"config\" to configure !");
                    return false;
                }
            } catch (IOException e) {
                StringUtils.printErr(e.getMessage());
            }
        }
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            properties.load(in);
            UPLOAD_FOLDER = properties.getProperty("UPLOAD_FOLDER_PATH");
            System.out.println("UPLOAD FOLDER: " + UPLOAD_FOLDER);
            String whitelist = properties.getProperty("WHITELIST_CLASSES");

            WHITELIST_CLASSES = whitelist.trim();
            System.out.println("Whitelisted Classes: " + WHITELIST_CLASSES);
            return true;
        } catch (IOException e) {
            StringUtils.printErr("Error loading configuration: " + e.getMessage());
            return false;
        }catch (NullPointerException e) {
            StringUtils.printErr("Error loading configuration: " + e.getMessage());
            return false;
        }


    }

    private static void saveConfigurationToFile() {
        Properties properties = new Properties();
        properties.setProperty("UPLOAD_FOLDER_PATH", UPLOAD_FOLDER);
        // Save the whitelisted classes to the config
        if (WHITELIST_CLASSES.equals("None")) {
            properties.setProperty("WHITELIST_CLASSES", "None");
        }
        properties.setProperty("WHITELIST_CLASSES", WHITELIST_CLASSES);
        System.out.println("CONFIG_FILE: " + CONFIG_FILE);
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "Agent Configuration");
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
        }
    }

    private static void doConfig() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the absolute path of the Upload folder: ");
        String uploadFolder = scanner.nextLine();
        System.out.print("Enter the dot-comma-separated list of whitelisted classes (e.g., ClassName1;ClassName2): ");
        String classes = scanner.nextLine();

        WHITELIST_CLASSES = (uploadFolder.isEmpty()) ? "None" : classes.trim();
        Contraints.UPLOAD_FOLDER = (uploadFolder.isEmpty()) ? "None" : uploadFolder;
        saveConfigurationToFile();
        StringUtils.println("Configuration saved!");
        scanner.close();


    }
}
