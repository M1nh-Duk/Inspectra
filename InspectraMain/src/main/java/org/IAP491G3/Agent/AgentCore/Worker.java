package org.IAP491G3.Agent.AgentCore;


import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.*;

import static org.IAP491G3.Agent.AgentCore.Filter.filterBlackList;
import static org.IAP491G3.Agent.AgentCore.MemoryTransformer.suspiciousClassAndMethod;
import static org.IAP491G3.Agent.Loader.Contraints.*;
import static org.IAP491G3.Agent.Loader.Contraints.WHITELIST_CLASS;
import static org.IAP491G3.Agent.Utils.ClassUtils.*;

import javassist.NotFoundException;
import org.IAP491G3.Agent.Loader.Contraints;
import org.IAP491G3.Agent.Utils.InstrumentationUtils;

import static org.IAP491G3.Agent.Utils.LogUtils.logToJSON;
import static org.IAP491G3.Agent.Utils.StringUtils.*;

import org.IAP491G3.Agent.Loader.AgentCache;
import org.IAP491G3.Agent.Utils.LogUtils;
import org.IAP491G3.Agent.Utils.StringUtils;
import org.IAP491G3.TaintAnalysis.TaintManager;

public class Worker {
    public static AgentCache workerAgentCache;
    protected static List<Class<?>> LOADED_CLASS = new ArrayList<>();
    private static Thread listeningThread;
    private static final TaintManager taintManager = new TaintManager();
    public static String testClass;

    public static void agentExecution(Instrumentation inst, AgentCache agentCache) {
        try {
            printLogo();
            System.out.println("========================= AGENT INIT ===============================");
//            StringUtils.println("11AgentCache class: " + agentCache.getClass().getName());
//            StringUtils.println("Worker classloader is " + Worker.class.getClassLoader());
            workerAgentCache = agentCache;
            StringUtils.println("Retransform loaded class ....");
            if (retransformLoadedClasses(inst)) {
                StringUtils.println("Retransform loaded class successfully !");
            } else {
                StringUtils.printAndLogErr(new RuntimeException("CANNOT RETRANSFORM LOADED CLASSES !"));
            }
            StringUtils.println("Adding memoryTransformer ....");
            MemoryTransformer memoryTransformer = new MemoryTransformer();
            memoryTransformer.setAgentCache(agentCache);
            inst.addTransformer(memoryTransformer, true);
            StringUtils.println("Add memoryTransformer successfully !");
            Set<ClassFileTransformer> test = InstrumentationUtils.invokeAgentCacheMethodWithCast(agentCache, "getTransformers", Set.class, false, inst);
            test.add(memoryTransformer);
            startActiveListeningThread(inst);
//
//            Set<ClassFileTransformer> test2 = invokeAgentCacheMethodWithCast(agentCache, "getTransformers", Set.class, false, inst);
//            for (ClassFileTransformer cl : test) {
//                StringUtils.println("Transformer: " + cl.toString());
//            }
//            activeListening(inst, false);
        } catch (Exception e) {
            StringUtils.printAndLogErr(e);
        }
    }


    public static boolean retransformLoadedClasses(Instrumentation inst) throws Exception {
        for (String suspiciousClass : suspiciousClassAndMethod.keySet()) {
            if (checkLoaded(inst, suspiciousClass)) {
                StringUtils.println("Loaded class: " + suspiciousClass);
                Class<?> loadedClassObj = getLoadedClassObjByFullPath(inst, suspiciousClass);
                if (loadedClassObj == null) {
//                    StringUtils.println("loadedClassObj is null");
                    continue;
                }

//                StringUtils.println("Loaded class obj: " + loadedClassObj.getName());
                if (loadedClassObj.isArray()) {
                    // Get the underlying component type, which represents the type of the array's elements
                    loadedClassObj = loadedClassObj.getComponentType();
                    // Get the simple name of the component type
                }
//                StringUtils.println("Loaded class obj after: " + loadedClassObj.getName());
                LOADED_CLASS.add(loadedClassObj);
            }
        }
        if (LOADED_CLASS != null) {
            MemoryTransformer memoryTransformer = new MemoryTransformer();
            memoryTransformer.setAgentCache(workerAgentCache);
            InstrumentationUtils.retransformClasses(inst, new MemoryTransformer(), LOADED_CLASS);
            return true;
        }
        return false;

    }

    public static void deleteActiveThread() {
        if (listeningThread != null && listeningThread.isAlive()) {
            listeningThread.interrupt();  // Request to stop the thread
            try {
                listeningThread.join(5000);  // Wait for the thread to finish its work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Preserve interrupt status
            }
            listeningThread = null;
        }

        // Other detachment logic here
        StringUtils.printAndLog("Thread deleted successfully.");
    }

    public static void startActiveListeningThread(Instrumentation inst) {
        listeningThread = new Thread(() -> {
            try {
                activeListening(inst);  // Your method that contains the while(true) loop
            } catch (Exception e) {
                printAndLogErr(e);
                StringUtils.printAndLog("Restarting active listening thread...");
                // Restart the thread in case of an exception
                deleteActiveThread();
                startActiveListeningThread(inst);
            }
        });
        listeningThread.setDaemon(true);  // Mark it as a daemon thread if you don't want it to prevent JVM from shutting down
        listeningThread.start();
    }

    public static void activeListening(Instrumentation inst) {

        System.out.println("========================= ACTIVE LISTENING ===============================");
        String exceptionKey = null;

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Iterator<String> iterator = getPropertiesIterator();
                while (iterator.hasNext()) {
                    String malKey = iterator.next();
                    exceptionKey = malKey;
                    StringUtils.println("Key: " + malKey);// + "Value: " + System.getProperty(malKey));
                    String value;
                    synchronized (System.getProperties()) {
                        value = System.getProperty(malKey).toString();
                    }
                    System.out.println("VALUE: "+value);
                    String tempClassList = malKey.split("__")[2];
                    String type = getType(malKey.split("__")[1]);
                    ArrayList<String> highlyMaliciousClass = getMalArray(tempClassList, value);
                    for (String className : highlyMaliciousClass) {
                        String fullPathclassName = className;
                        if (isWhitelist(fullPathclassName)){
//                            StringUtils.printAndLog("Class " + className + " is whitelisted");
                            continue;
                        }
//                        System.out.println("fullpath: " + fullPathclassName);
                        Result classResult = new Result(fullPathclassName, type);
                        Map<String, List<String>> filterResult = null;
                        boolean classIsJSP = false;
                        if (className.toLowerCase().contains("standardcontext")) {
                            continue;
                        }

                        StringUtils.printAndLog("==============> FOUND SUSPICIOUS CLASS: " + fullPathclassName);

                        if (className.contains("_jsp")) {
                            className = className.substring(className.lastIndexOf(".") + 1);
                            classIsJSP = true;
                        }
                        System.out.println("Clas name: " + className);
//                        StringUtils.println("Temp class Name: " + fullPathclassName);
                        Class<?> tempClass = getLoadedClassObjByCLassName(inst, className);
                        if (tempClass == null) {
                            System.out.println("TEMPCLASS IS NULL");
                        }
                        if (InstrumentationUtils.isClassAlreadyTransformed(tempClass)) {
                            StringUtils.printAndLog("Class already destroyed ! Aborting this class .... ");
                            continue;
                        }
                        StringUtils.println("FOUND CLASS OBJECT FOR: " + tempClass.getName());
                        dumpClass(inst, fullPathclassName);


//                        if (!classIsJSP) {
//                            dumpClass(inst, className, DUMP_DIR);
//                        }
                        className = className.substring(className.lastIndexOf(".") + 1);
                        boolean taintResult = taintManager.taint(className, DUMP_DIR);
                        classResult.setTaintResult(taintResult);
                        if (taintResult) {
                            classResult.setRiskScore(5);
                            StringUtils.println("TAINT RESULT: CLASS IS MALICIOUS");
                        }
                        if (classResult.getRiskScore() < 5) {
                            String classOutputPath;
//                            classOutputPath = (classIsJSP) ? getOutputPath(className, JSP_FOLDER) : getOutputPath(className, DUMP_DIR);
                            classOutputPath = getOutputPath(className, DUMP_DIR);

//                            StringUtils.printAndLog("classOutputPath: " + classOutputPath);

                            String decompiledClass = decompileClass(classOutputPath, null, false);
                            if (decompiledClass.isEmpty()) {
                                printAndLogErr(new Exception("Cannot decompile class " + className));
                                continue;
                            }
                            filterResult = filterBlackList(decompiledClass, className, true);
                            int tempRiskScore = classResult.getRiskScore();
                            tempRiskScore += Integer.valueOf(filterResult.get("RiskScore").get(0));
                            classResult.setRiskScore(tempRiskScore);
                            classResult.setFilterResult(filterResult);
                        }

                        if (classResult.getRiskScore() >= 5) {
                            boolean retransformStatus = retransformMalClass(inst, fullPathclassName, tempClass);
                            classResult.setRetransformedStatus(retransformStatus);
                            if (classIsJSP) {
                                classResult.setJSP(classIsJSP);
                                Boolean JspDeleteStatus =false;// deleteJspFile(className);
                                classResult.setJspDeleteStatus(JspDeleteStatus);

                            }
                        }

                        handleResult(classResult);
                    }
                    iterator.remove();
                    synchronized (System.getProperties()) {
                        if (!(System.getProperty(malKey) == null)) {
                            System.clearProperty(malKey);
                        }
                    }
                    Thread.sleep(1000);  // Replace with actual wait logic if applicable
                }

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Preserve interrupt status
        } catch (RuntimeException e) {
            synchronized (System.getProperties()) {
                if (!(System.getProperty(exceptionKey) == null)) {
                    System.clearProperty(exceptionKey);
                }
            }
            throw new RuntimeException(e);
        } catch (NotFoundException | UnmodifiableClassException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Iterator<String> getPropertiesIterator() {
        ArrayList<String> malProperties = new ArrayList<>();
        Properties properties = System.getProperties();
        synchronized (properties) { // Synchronize access to properties
            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String key = (String) propertyNames.nextElement();
                if (key.startsWith("MAL")) {
                    malProperties.add(key);
                }
            }
        }

        Iterator<String> iterator = malProperties.iterator();
        return iterator;
    }
    private static boolean isWhitelist (String className){
        if (className.startsWith("org.apache.jsp.uploads")){
            return false;
        }
        if (className.startsWith("org.apache") || className.startsWith("javax") || className.startsWith("java")){
            return true;
        }
        for (String wl : WHITELIST_CLASS){
            if (className.startsWith(wl)){
                return true;
            }
        }
        return false;
    }
    private static ArrayList<String> getMalArray(String tempClassList, String value) {
        ArrayList<String> highlyMaliciousClass = new ArrayList<String>();
        if (tempClassList.contains(",")) {
            for (String item : tempClassList.split(",")) {
                highlyMaliciousClass.add(item.trim());
            }
        } else {
            highlyMaliciousClass.add(tempClassList);
        }
        if (value.startsWith("class")) {
            value = value.substring(value.indexOf("class") + "class".length()).trim();
            highlyMaliciousClass.add(value);
        }
        highlyMaliciousClass.add(value);
        return highlyMaliciousClass;
    }

    private static String getType(String methodName) {
        String[] type = {"Tomcat Filter", "Tomcat Listener", "Tomcat Servlet", "Spring Controller", "Spring Interceptor", "Generic"};
        if (methodName.equals("setFilterClass")) {
            return type[0];
        }
        if (methodName.equals("addApplicationEventListener")) {
            return type[1];
        }
        if (methodName.equals("addServletMappingDecoded") || methodName.equals("setServletClass")) {
            return type[2];
        }
        if (methodName.equals("setFilterClass")) {
            return type[3];
        }
        if (methodName.equals("setFilterClass")) {
            return type[4];
        }
        return type[5];

    }

    private static boolean retransformMalClass(Instrumentation inst, String className, Class<?> clazzObj) throws
            UnmodifiableClassException, RuntimeException {
        if (isWhitelist(className)){
            StringUtils.printAndLog("Class " + className + " is whitelisted");
            return false;
        }
        if (!OPTION_AUTO_DELETE) {
            Scanner scanner = new Scanner(System.in); // Create a scanner object for user input
            StringUtils.println("Do you want to retransform this class? (Y/N): ");
            String userInput = scanner.nextLine().trim().toUpperCase();
//            scanner.close();
//            scanner = null;
            if ("N".equals(userInput)) {
                StringUtils.println("Class [" + className + "] will not be retransformed.");
                return false;
            }
        }
        StringUtils.println("Retransforming class [" + className + "] .....");
        ArrayList<Class<?>> classList = new ArrayList<>();
        classList.add(clazzObj);

        InstrumentationUtils.retransformClasses(inst, new DestroyTransformer(className), classList);
//        if (destroyTransformer == null){
//            destroyTransformer = new DestroyTransformer();
//            inst.addTransformer(destroyTransformer,true);
//        }
////        destroyTransformer.setMalClassName(className);
//        testClass = className;
//        inst.retransformClasses(clazzObj);
        StringUtils.printAndLog("Retransforming class [" + className + "] successfully !");
        return true;
    }

    private static boolean handleResult(Result result) {
        int riskScore = result.getRiskScore();
        StringUtils.printAndLog("RESULT FOR CLASS: " + result.getFullPathClassName() + "|| RISK SCORE: " + riskScore);
        if (riskScore <= 1) {
            StringUtils.printAndLog("Class is not malicious");
        } else if (riskScore >= 5) {
            StringUtils.printAndLog("Class is malicious");
        } else {
            StringUtils.printAndLog("Class is suspicious");
        }
        Map<String, List<String>> filterResult = result.getFilterResult();
        if (!(filterResult == null) && !filterResult.isEmpty()) {
            // Using StringBuilder to collect the formatted result
            StringBuilder logContent = new StringBuilder();
            // Adding a title
            logContent.append("==> Matched Keywords:\n");
            // Iterating through the results and appending to the StringBuilder
            filterResult.forEach((key, value) -> {
                if (value.isEmpty()) {
                    logContent.append(key).append(": None\n");
                } else {
                    logContent.append(key).append(": ").append(value).append("\n");
                }
            });

            // Writing the collected log content to the file
            LogUtils.logit(logContent.toString());
        }
        if (riskScore >= 5) {
            logToJSON(result);
        }
        return true;
    }

    private static Boolean deleteJspFile(String className) {
        if (UPLOAD_FOLDER == null || UPLOAD_FOLDER.equals("None")) {
            StringUtils.printErr("UPLOAD FOLDER IS NOT CONFIGURED. CANNOT DELETE JSP FILE !");
            return false;
        }
        if (className.startsWith("_")) {
            className = className.substring(className.indexOf("_") + 1);
        }
        className = className.replace("_jsp", ".jsp");
        className = className.substring(0, className.indexOf(".jsp")+4);
        String jspPath = UPLOAD_FOLDER + File.separator + className;
//        System.out.println("jspPath: " + jspPath);
        File jspFile = new File(jspPath);
        if (jspFile.exists()) {
            if (jspFile.isFile()) {
                if (!OPTION_AUTO_DELETE) {
                    Scanner scanner = new Scanner(System.in); // Create a scanner object for user input
                    StringUtils.println("Malicious JSP file detected at: " + jspPath);
                    StringUtils.println("Do you want to delete this JSP file? (Y/N): ");
                    String userInput = scanner.nextLine().trim().toUpperCase();
                    scanner.close();
                    scanner = null;
                    if ("N".equals(userInput)) {
                        StringUtils.println("File JSP at path \"" + jspPath + "\" will not be deleted.");
                        return false;
                    }
                }
                StringUtils.println("Delete JSP file [" + className + "] .....");
                if (jspFile.delete()) {
                    StringUtils.println(jspFile.getName() + " has been deleted successfully.");
                    return true;
                } else {
                    StringUtils.printAndLogErr(new Exception("Failed to delete " + jspFile.getName()));
                }
            } else {
                StringUtils.printAndLogErr(new Exception(jspFile.getName() + " is not a valid file."));
            }
        } else {
            StringUtils.printAndLog(jspFile.getName() + " does not exist or already deleted!");
        }
        return false;
    }



    public static void configureOption(String args) {
        String[] tempArgs= convertToStringArray(args);
        for (String arg : tempArgs) {
            System.out.println("ARGS: " + arg);
            if ("-silent".equalsIgnoreCase(arg)) {
                OPTION_SILENT = true;
            } else if ("-auto".equalsIgnoreCase(arg)) {
                OPTION_AUTO_DELETE = true;
            }
        }
        UPLOAD_FOLDER = tempArgs[tempArgs.length - 1];
        String whitelistClass = tempArgs[tempArgs.length - 2];
//        WHITELIST_CLASS.add("org.IAP491");

        if (!whitelistClass.equals("None")){
            if (whitelistClass.contains(";")) {
                WHITELIST_CLASS.addAll(Arrays.asList(whitelistClass.split(";")));
            }
            else {
                WHITELIST_CLASS.add(whitelistClass);
            }
        }
        for (String temp: WHITELIST_CLASS){
            System.out.println("TEMP: " + temp);
        }


    }
}
