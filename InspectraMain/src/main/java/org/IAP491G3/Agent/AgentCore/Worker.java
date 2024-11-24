package org.IAP491G3.Agent.AgentCore;

import javassist.*;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.*;

import static org.IAP491G3.Agent.AgentCore.Filter.filterBlackList;
import static org.IAP491G3.Agent.AgentCore.MemoryTransformer.suspiciousClassAndMethod;
import static org.IAP491G3.Agent.Loader.Contraints.*;
import static org.IAP491G3.Agent.Utils.ClassUtils.*;

import org.IAP491G3.Agent.Utils.InstrumentationUtils;

import static org.IAP491G3.Agent.Utils.PathUtils.createDumpFolder;
import static org.IAP491G3.Agent.Utils.StringUtils.*;

import org.IAP491G3.Agent.Loader.AgentCache;
import org.IAP491G3.Agent.Utils.LogUtils;
import org.IAP491G3.Agent.Utils.PathUtils;
import org.IAP491G3.Agent.Utils.StringUtils;
import org.IAP491G3.TaintAnalysis.TaintManager;

public class Worker {
    public static AgentCache workerAgentCache;
    protected static List<Class<?>> LOADED_CLASS = new ArrayList<>();
    private static Thread listeningThread;
    private static final TaintManager taintManager = new TaintManager();
    static Scanner scanner = new Scanner(System.in); // Create a scanner object for user input

    public static void agentExecution(Instrumentation inst, AgentCache agentCache) {
        try {
            printLogo();
            System.out.println("========================= AGENT INIT ===============================");
//            StringUtils.println("11AgentCache class: " + agentCache.getClass().getName());
//            StringUtils.println("Worker classloader is " + Worker.class.getClassLoader());
            workerAgentCache = agentCache;
            DUMP_DIR = PathUtils.getCurrentDirectory() + File.separator + "dump";
            createDumpFolder();
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


    public static boolean retransformLoadedClasses(Instrumentation inst) throws UnmodifiableClassException {
        for (String suspiciousClass : suspiciousClassAndMethod.keySet()) {
            if (checkLoaded(inst, suspiciousClass)) {
                StringUtils.println("Loaded class: " + suspiciousClass);
                Class<?> loadedClassObj = getLoadedClassObjByCLassName(inst, suspiciousClass);
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
                listeningThread.join();  // Wait for the thread to finish its work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Preserve interrupt status
            }
            listeningThread = null;
        }

        // Other detachment logic here
        StringUtils.println("Thread deleted successfully.");
    }

    public static void startActiveListeningThread(Instrumentation inst) {
        listeningThread = new Thread(() -> {
            try {
                activeListening(inst);  // Your method that contains the while(true) loop
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        listeningThread.setDaemon(true);  // Mark it as a daemon thread if you don't want it to prevent JVM from shutting down
        listeningThread.start();
    }

    public static void activeListening(Instrumentation inst) {
        System.out.println("========================= ACTIVE LISTENING ===============================");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int RISK_SCORE = 0;
                ArrayList<String> malProperties = new ArrayList<>();
                Properties properties = System.getProperties();
                Enumeration<?> propertyNames = properties.propertyNames();
                while (propertyNames.hasMoreElements()) {
                    String key = (String) propertyNames.nextElement();
                    if (key.startsWith("MAL")) {
                        malProperties.add(key);
                    }
                }

                Iterator<String> iterator = malProperties.iterator();
                while (iterator.hasNext()) {
                    String malKey = iterator.next();
//                    StringUtils.println("key: " + malKey);
//                    StringUtils.println(key + ": " + entry.getValue());
                    String value = System.getProperty(malKey).toString();
                    String tempClassList = malKey.split("__")[1];

                    ArrayList<String> highlyMaliciousClass = new ArrayList<String>();
                    if (tempClassList.contains(",")) {
                        for (String item : tempClassList.split(",")) {
                            highlyMaliciousClass.add(item.trim());
                        }
                    } else {
                        highlyMaliciousClass.add(tempClassList);
                    }
                    for (String className : highlyMaliciousClass) {
                        boolean classIsJSP = false;
                        if (className.toLowerCase().contains("standardcontext")) {
                            iterator.remove();
                            System.clearProperty(malKey);
                            continue;
                        }
                        if (className.contains("$")) {
                            className = className.substring(0, className.indexOf("$"));
                        }
                        String fullPathclassName = className;
                        StringUtils.printAndLog("==============> FOUND SUSPICIOUS CLASS: " + fullPathclassName);

                        if (className.contains("_jsp")) {
//                                className = className.substring(className.indexOf("_"));
                            className = className.substring(className.lastIndexOf("."));
                            classIsJSP = true;
//                                continue;
                        }

//                        StringUtils.println("Temp class Name: " + fullPathclassName);

                        Class<?> tempClass = getLoadedClassObjByCLassName(inst, className);
                        if (InstrumentationUtils.isClassAlreadyTransformed(tempClass)) {
                            StringUtils.printAndLog("Class already destroyed ! Aborting this class .... ");
                            iterator.remove();
                            System.clearProperty(malKey);
                            continue;
                        }
//                        StringUtils.println("FOUND CLASS OBJECT FOR: " + tempClass.getName());
                        if (!classIsJSP) {
                            dumpClass(inst, className, DUMP_DIR);
                        }
                        className = className.substring(className.lastIndexOf(".") + 1);

                        boolean taintResult = taintManager.taint(className, new ArrayList<>(Arrays.asList(JSP_FOLDER, DUMP_DIR)));
                        if (taintResult) {
                            RISK_SCORE = 5;
//                            StringUtils.println("TAINT RESULT: CLASS IS MALICIOUS");
                        }
                        Map<String, List<String>> filterResult = null;
                        if (RISK_SCORE < 5) {
                            String classOutputPath;
                            classOutputPath = (classIsJSP) ? getOutputPath(className, JSP_FOLDER) : getOutputPath(className, DUMP_DIR);
                            StringUtils.printAndLog("classOutputPath: " + classOutputPath);
                            String decompiledClass = decompileClass(classOutputPath, null, false);
//                            StringUtils.println(decompiledClass);
//                            StringUtils.println("========================================= AFTER +++=++++++++++++++++++++");
                            filterResult = filterBlackList(decompiledClass, className, true);
                            RISK_SCORE += Integer.valueOf(filterResult.get("RiskScore").get(0));
                        }
                        handleResult(fullPathclassName, RISK_SCORE, filterResult);
                        // Ask the user for input to delete the malicious class
                        retransformMalClass(inst, fullPathclassName, tempClass);
                        if (classIsJSP){
                            deleteJspFile(className);
                        }
                        iterator.remove();
                        System.clearProperty(malKey);
                    }
                    Thread.sleep(1000);  // Replace with actual wait logic if applicable
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Preserve interrupt status
        } catch (Exception e) {
            StringUtils.printAndLogErr(new Exception("Unexpected error: "));
        }
    }

    /*
        public static void activeListening(Instrumentation inst) throws NotFoundException, IOException, CannotCompileException {
            StringUtils.println("========================= ACTIVE LISTENING ==============");
    //        try {
                while (!Thread.currentThread().isInterrupted()) {
                    final int[] RISK_SCORE = {0};
                    ArrayList<String> delKey = new ArrayList<>();
                    final Properties[] properties = {new Properties()};
                    properties[0].putAll(System.getProperties());
                    // Iterate over properties to find keys that start with "MAL"
    //                StringUtils.println("System properties starting with 'MAL':");
                    properties[0].entrySet().stream()
                            .filter(entry -> String.valueOf(entry.getKey()).startsWith("MAL"))
                            .forEach(entry -> {
                                try {
    //                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                                String key = String.valueOf(entry.getKey());
                                StringUtils.println("key: " + key);
    //                    StringUtils.println(key + ": " + entry.getValue());
                                if (key.startsWith("MAL")) {
                                    String value = entry.getValue().toString();
                                    StringUtils.println("======================================= FOUND MAL CLASS2");
                                    StringUtils.println(key + " = " + value);
    //                        Class<?> tempClass = null;
                                    String tempClassName = key.split("__")[1];

                                    ArrayList<String> highlyMaliciousClass = new ArrayList<String>();
                                    if (tempClassName.contains(",")) {
                                        for (String item : tempClassName.split(",")) {
                                            highlyMaliciousClass.add(item.trim());
                                        }
                                    } else {
                                        highlyMaliciousClass.add(tempClassName);
                                    }
                                    for (String className : highlyMaliciousClass) {
                                        boolean classIsJSP = false;

                                        if (className.toLowerCase().contains("standardcontext")) {
                                            continue;
                                        }
                                        if (className.contains("_jsp")) {
    //                                className = className.substring(className.indexOf("_"));
                                            className = className.substring(className.lastIndexOf("."));
                                            classIsJSP = true;
    //                                continue;
                                        }
                                        if (className.contains("$")) {
                                            className = className.substring(0, className.indexOf("$"));
                                        }
                                        StringUtils.println("Temp class Name: " + className);
    //                            tempClass = getLoadedClassObjByCLassName(inst, className);
                                        if (!classIsJSP) {

                                                dumpClass(inst, className, DUMP_DIR);

                                        }


                                        className = className.substring(className.lastIndexOf(".") + 1);
                                        StringUtils.println("FOUND CLASS OBJECT FOR: " + className);
                                        boolean taintResult = taintManager.taint(className, classPathForSoot);
                                        if (taintResult) {
                                            RISK_SCORE[0] = 5;
                                            StringUtils.println("TAINT RESULT: CLASS IS MALICIOUS");
                                        }
                                        if (RISK_SCORE[0] < 5) {
                                            String classOutputPath;
                                            classOutputPath = (classIsJSP) ? getOutputPath(className, JSP_FOLDER) : getOutputPath(className, DUMP_DIR);
                                            StringUtils.println("classOutputPath: " + classOutputPath);
                                            String decompiledClass = decompileClass(classOutputPath, null, false);
    //                            StringUtils.println(decompiledClass);
    //                            StringUtils.println("========================================= AFTER +++=++++++++++++++++++++");
    //                            decompiledClass = removeCommentOfDecompileClass(decompiledClass);
                                            Map<String, List<String>> filterResult = filterBlackList(decompiledClass, className, true);
                                            RISK_SCORE[0] += Integer.valueOf(filterResult.get("RiskScore").get(0));
                                        }
                                        handleResult(className, RISK_SCORE[0], null);
                                        delKey.add(key);

                                    }
                                }
                                properties[0] = null;   // Dereference the copied properties object to get a new one in the next iteration

                                    Thread.sleep(1000);  // Replace with actual wait logic if applicable
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();  // Preserve interrupt status
                                } catch (NotFoundException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    for (String key : delKey) {
                        System.clearProperty(key);

                    }

                }
    //        } catch (InterruptedException e) {
    //            // Handle interruption
    //        } catch (Exception e) {
    //            System.err.println("Unexpected error: " + e.getMessage());
    //        }
    //catch (Exception e) {
    //            throw new RuntimeException(e);
    //        }
        }
        */
    private static void retransformMalClass(Instrumentation inst, String className, Class<?> clazzObj) throws UnmodifiableClassException {
        if (!AUTO_DELETE) {
            StringUtils.println("Do you want to retransform this class? (Y/N): ");
            String userInput = scanner.nextLine().trim().toUpperCase();
            if ("N".equals(userInput)) {
                StringUtils.println("Class [" + className + "] will not be retransformed.");
                return;
            }
        }
        StringUtils.println("Retransforming class [" + className + "] .....");
        List<Class<?>> classList = new ArrayList<>();
        classList.add(clazzObj);
        InstrumentationUtils.retransformClasses(inst, new DestroyTransformer(className), classList);
        StringUtils.printAndLog("Retransforming class [" + className + "] successfully !");
    }

    private static boolean handleResult(String className, int riskScore, Map<String, List<String>> filterResult) {
        StringUtils.printAndLog("RESULT");
        StringUtils.printAndLog("Class: " + className + "|| Risk Score: " + riskScore);
        if (riskScore <= 1) {
            StringUtils.printAndLog("Class is not malicious");
        } else if (riskScore >= 5) {
            StringUtils.printAndLog("Class is malicious");
        } else {
            StringUtils.printAndLog("Class is suspicious");
        }
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
        return true;
    }

    private static void deleteJspFile(String className) {
        if (className.startsWith("_")){
            className = className.substring(className.indexOf("_") + 1);
        }
        className = className.replace("_jsp",".jsp");
        String jspPath = UPLOAD_FOLDER + File.separator + className;
        File jspFile = new File(jspPath);
        if (jspFile.exists()) {
            if (jspFile.isFile()) {
                if (!AUTO_DELETE) {
                    StringUtils.println("Malicious JSP file detected at: " + jspPath);
                    StringUtils.println("Do you want to delete this JSP file? (Y/N): ");
                    String userInput = scanner.nextLine().trim().toUpperCase();
                    if ("N".equals(userInput)) {
                        StringUtils.println("File JSP at path \"" + jspPath + "\" will not be deleted.");
                        return;
                    }
                }
                StringUtils.println("Delete JSP file [" + className + "] .....");
                if (jspFile.delete()) {
                    StringUtils.println(jspFile.getName() + " has been deleted successfully.");
                } else {
                    StringUtils.printAndLogErr(new Exception("Failed to delete " + jspFile.getName()));
                }
            } else {
                StringUtils.printAndLogErr(new Exception(jspFile.getName() + " is not a valid file."));
            }
        } else {
            StringUtils.printAndLogErr(new Exception(jspFile.getName() + " does not exist."));
        }
    }

    public static void printLoadedClass(Instrumentation inst) {
        StringUtils.println("All loaded classes: ");
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz != null) {
                try {
                    // Get class
//                    if (clazz.getPackage().toString().contains("org.example")) {
//                        String className = clazz.toString();
//                        StringUtils.println(className);
//                    }
                    StringUtils.println("Class: " + clazz.getName() + ", Class Loader: " + clazz.getClassLoader());
                } catch (Exception e) {
                    System.err.println("Error finding class: " + e.getMessage());
                }
//
            }
        }

    }
}
