package org.IAP491G3.Agent.Loader;

import java.util.ArrayList;
import java.util.Arrays;

import static org.IAP491G3.Agent.Utils.PathUtils.getCurrentDirectory;

public class Contraints {

    public static final String JAVA_INTERNAL_PACKAGES = "^(java|javax|jakarta|(com\\.)?sun)\\..*";

    public static final String AGENT_NAME = "Inspectra";
    public static final String  FILE_SEPERATOR = System.getProperty("file.separator");
    public static  String CONFIG_FILE=getCurrentDirectory() + FILE_SEPERATOR + "config.properties";
    public static final String AGENT_LOADER_FILE_NAME = AGENT_NAME + ".jar";
    public static  String DUMP_DIR ;
    public static   String UPLOAD_FOLDER ;
    public static  boolean OPTION_SILENT = false  ;
    public static  boolean OPTION_AUTO_DELETE = false;
    public static ArrayList<String> WHITELIST_CLASS = new ArrayList<>(Arrays.asList("org.IAP491", "com.google.gson", "org.eclipse.jdt.internal"));

}