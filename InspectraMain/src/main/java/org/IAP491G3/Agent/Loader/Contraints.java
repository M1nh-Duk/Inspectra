package org.IAP491G3.Agent.Loader;

import java.io.File;

public class Contraints {

    public static final String JAVA_INTERNAL_PACKAGES = "^(java|javax|jakarta|(com\\.)?sun)\\..*";

    public static final String AGENT_NAME = "Inspectra";

    public static final String ENCODING = "UTF-8";

    public static final String AGENT_FILE_NAME = AGENT_NAME + "-AgentCore.jar";

    public static final String AGENT_LOADER_FILE_NAME = AGENT_NAME + "-loader.jar";
    public static final String JSP_FOLDER = "C:\\Users\\Minh Duc\\.SmartTomcat\\TomcatJDK8\\TomcatJDK8\\work\\Catalina\\localhost\\TomcatJDK8\\org\\apache\\jsp\\uploads";
    public static final String OS_VERSION = System.getProperty("os.name");
    public static final String DUMP_DIR = System.getProperty("user.dir") + File.separator + "dump";;
    public static final boolean AUTO_DELETE = true;




}