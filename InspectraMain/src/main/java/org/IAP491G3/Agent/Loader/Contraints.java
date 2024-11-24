package org.IAP491G3.Agent.Loader;

import org.IAP491G3.Agent.Utils.PathUtils;

import java.io.File;

public class Contraints {

    public static final String JAVA_INTERNAL_PACKAGES = "^(java|javax|jakarta|(com\\.)?sun)\\..*";

    public static final String AGENT_NAME = "Inspectra";

    public static final String AGENT_FILE_NAME = AGENT_NAME + "-AgentCore.jar";

    public static final String AGENT_LOADER_FILE_NAME = AGENT_NAME + "-loader.jar";
    public static final String JSP_FOLDER = "C:\\Users\\Minh Duc\\.SmartTomcat\\TomcatJDK8\\TomcatJDK8\\work\\Catalina\\localhost\\TomcatJDK8\\org\\apache\\jsp\\uploads";
    public static final String OS_VERSION = System.getProperty("os.name");
    public static  String DUMP_DIR ;
    public static final boolean AUTO_DELETE = true;
    public static  final String UPLOAD_FOLDER =  "D:\\IntelliJ project\\TomcatJDK8\\src\\main\\java\\org\\example\\uploads";



}