package org.IAP491G3.Agent.Loader;

import org.IAP491G3.Agent.Utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VMProxy {

    private Class<?> virtualMachineClass;

    public VMProxy() {
        String virtualMachineName = "com.sun.tools.attach.VirtualMachine";

        try {
            virtualMachineClass = Class.forName(virtualMachineName);
        } catch (ClassNotFoundException e) {
            File toolsJar = VMProxy.getJDKToolsJar();

            try {
                URL[] urls = new URL[]{toolsJar.toURI().toURL()};
                virtualMachineClass = new URLClassLoader(urls).loadClass(virtualMachineName);
            } catch (Exception ex) {
                StringUtils.printErr("Load tools.jar Exception: " + e);
                ex.printStackTrace();
            }
        }
    }


    public static File getJDKToolsJar() {
        String javaHome = System.getenv().get("JAVA_HOME");
        File   jarFile  = null;

        if (javaHome != null) {
            jarFile = new File(javaHome);
        } else {
            jarFile = new File(System.getProperty("java.home"));
        }

        File toolsJar = new File(jarFile + "/lib/", "tools.jar");

        if (!toolsJar.exists()) {
            toolsJar = new File(toolsJar.getParentFile().getParentFile() + "/lib/", "tools.jar");
        }

        if (!toolsJar.exists()) {
            toolsJar = new File(toolsJar.getParentFile().getParentFile().getParentFile() + "/lib/", "tools.jar");
        }

        if (toolsJar.exists()) {
            return toolsJar;
        } else {
            toolsJar = new File(jarFile + "/Classes/", "classes.jar");

            if (toolsJar.exists()) {
                return toolsJar;
            }
        }

        throw new RuntimeException("Can Not Load JVM tools.jar,Please Confirm Your \"JAVA_HOME\" Config");
    }


    public Map<String, String> listJvmPid() throws Exception {
        Map<String, String> processMap = new HashMap<String, String>();
        List<?>             list       = (List<?>) virtualMachineClass.getDeclaredMethod("list").invoke(null);

        for (Object p : list) {
            Class<?> descriptorClass = p.getClass();
            String   processId       = (String) descriptorClass.getMethod("id").invoke(p);
            String   displayName     = (String) descriptorClass.getMethod("displayName").invoke(p);

            processMap.put(processId, displayName);
        }

        return processMap;
    }


    public Object attach(String pid) throws Exception {
        return virtualMachineClass.getDeclaredMethod("attach", String.class).invoke(null, pid);
    }

    public void detach(Object vm) throws Exception {
        vm.getClass().getDeclaredMethod("detach").invoke(vm);
    }


    public void loadAgent(Object vm, String agentFile, String args) throws Exception {
        virtualMachineClass.getDeclaredMethod("loadAgent", String.class, String.class).invoke(vm, agentFile, args);
    }

}