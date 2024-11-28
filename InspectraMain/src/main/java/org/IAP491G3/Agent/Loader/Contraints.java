package org.IAP491G3.Agent.Loader;

import org.IAP491G3.Agent.Utils.PathUtils;

import java.io.File;

public class Contraints {

    public static final String JAVA_INTERNAL_PACKAGES = "^(java|javax|jakarta|(com\\.)?sun)\\..*";

    public static final String AGENT_NAME = "Inspectra";
    public static final String  FILE_SEPERATOR = System.getProperty("file.separator");
    public static  String CONFIG_FILE;
    public static final String AGENT_LOADER_FILE_NAME = AGENT_NAME + "-loader.jar";
    public static  String DUMP_DIR ;
    public static   String UPLOAD_FOLDER ;
    public static  boolean OPTION_SILENT = false  ;
    public static  boolean OPTION_AUTO_DELETE = true;

}