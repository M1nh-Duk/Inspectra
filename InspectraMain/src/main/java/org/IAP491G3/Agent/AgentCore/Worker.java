package org.IAP491G3.Agent.AgentCore;

import javassist.*;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.*;

import static org.IAP491G3.Agent.AgentCore.Filter.filterBlackList;
import static org.IAP491G3.Agent.AgentCore.MemoryTransformer.suspiciousClassAndMethod;
import static org.IAP491G3.Agent.Loader.Contraints.*;
import static org.IAP491G3.Agent.Utils.ClassUtils.*;

import org.IAP491G3.Agent.Utils.InstrumentationUtils;

import static org.IAP491G3.Agent.Utils.StringUtils.*;

import org.IAP491G3.Agent.Loader.AgentCache;
import org.IAP491G3.TaintAnalysis.TaintManager;

public class Worker {
    public static AgentCache workerAgentCache;
    protected static List<Class<?>> LOADED_CLASS = new ArrayList<>();
    private static Thread listeningThread;
    private static final TaintManager taintManager = new TaintManager();
    private static final ArrayList<String> classPathForSoot = new ArrayList<>(Arrays.asList(JSP_FOLDER, DUMP_DIR));
    static Scanner scanner = new Scanner(System.in); // Create a scanner object for user input

    public static void agentExecution(Instrumentation inst, AgentCache agentCache) throws Exception {
        try {
            printLogo();
            System.out.println("========================= AGENT EXECUTION ==============");
//            System.out.println("11AgentCache class: " + agentCache.getClass().getName());
//            System.out.println("Worker classloader is " + Worker.class.getClassLoader());
            workerAgentCache = agentCache;
            createDumpFolder();
            handleLoadedClasses(inst);
            System.out.println("=========> Handled loaded class successfully !");
            System.out.println("========================= ADDING TRANSFORMER ==============");
            MemoryTransformer memoryTransformer = new MemoryTransformer();
            memoryTransformer.setAgentCache(agentCache);
            inst.addTransformer(memoryTransformer, true);
            System.out.println("=========> Add memoryTransformer successfully !");
            Set<ClassFileTransformer> test = InstrumentationUtils.invokeAgentCacheMethodWithCast(agentCache, "getTransformers", Set.class, false, inst);
            test.add(memoryTransformer);
            startActiveListeningThread(inst);
//
//            Set<ClassFileTransformer> test2 = invokeAgentCacheMethodWithCast(agentCache, "getTransformers", Set.class, false, inst);
//            for (ClassFileTransformer cl : test) {
//                System.out.println("Transformer: " + cl.toString());
//            }
//            activeListening(inst, false);
        } catch (Exception e) {
            System.err.println("Error in agentExecution: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void handleLoadedClasses(Instrumentation inst) {
        System.out.println("========================= HANDLING LOADED CLASSES ==============");
        for (String suspiciousClass : suspiciousClassAndMethod.keySet()) {
            if (checkLoaded(inst, suspiciousClass)) {
                System.out.println("suspicious class: " + suspiciousClass);
                Class<?> loadedClassObj = getLoadedClassObjByCLassName(inst, suspiciousClass);
                if (loadedClassObj == null) {
                    System.out.println("loadedClassObj is null");
                    continue;
                }

                System.out.println("Loaded class obj: " + loadedClassObj.getName());
                if (loadedClassObj.isArray()) {
                    // Get the underlying component type, which represents the type of the array's elements
                    loadedClassObj = loadedClassObj.getComponentType();
                    // Get the simple name of the component type
                }
//                System.out.println("Loaded class obj after: " + loadedClassObj.getName());
                LOADED_CLASS.add(loadedClassObj);
            }
        }
        if (LOADED_CLASS != null) {
            MemoryTransformer memoryTransformer = new MemoryTransformer();
            System.out.println("My workerAgentCache: " + workerAgentCache);
            memoryTransformer.setAgentCache(workerAgentCache);
            InstrumentationUtils.retransformClasses(inst, new MemoryTransformer(), LOADED_CLASS);
        }
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
        System.out.println("Thread deleted successfully.");
    }

    public static void startActiveListeningThread(Instrumentation inst) {
        listeningThread = new Thread(() -> {
            try {
                activeListening(inst);  // Your method that contains the while(true) loop
            } catch (NotFoundException | IOException | CannotCompileException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        listeningThread.setDaemon(true);  // Mark it as a daemon thread if you don't want it to prevent JVM from shutting down
        listeningThread.start();
    }

    public static void activeListening(Instrumentation inst) throws NotFoundException, IOException, CannotCompileException, InterruptedException {
        System.out.println("========================= ACTIVE LISTENING ==============");
//        try {
        while (!Thread.currentThread().isInterrupted()) {
            int RISK_SCORE = 0;
            ArrayList<String> malProperties = new ArrayList<>();
            Properties properties = System.getProperties();
            // Iterate over properties to find keys that start with "MAL"
//                System.out.println("System properties starting with 'MAL':");
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
                System.out.println("key: " + malKey);
//                    System.out.println(key + ": " + entry.getValue());
                String value = System.getProperty(malKey).toString();
                System.out.println("======================================= FOUND MAL CLASS2");
                System.out.println(malKey + " = " + value);
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
                    String fullPathclassName =  className;

                    if (className.contains("_jsp")) {
//                                className = className.substring(className.indexOf("_"));
                        className = className.substring(className.lastIndexOf("."));
                        classIsJSP = true;
//                                continue;
                    }


                    System.out.println("Temp class Name: " + fullPathclassName);

                    Class<?> tempClass =  getLoadedClassObjByCLassName(inst, className);
                    if (InstrumentationUtils.isClassAlreadyTransformed(tempClass)){
                        System.out.println("Class already destroyed ! Aborting .... ");
                        iterator.remove();
                        System.clearProperty(malKey);
                        continue;
                    }
                    System.out.println("FOUND CLASS OBJECT FOR: " + tempClass.getName());
                    if (!classIsJSP) {
                        dumpClass(inst, className, DUMP_DIR);
                    }

                    className = className.substring(className.lastIndexOf(".") + 1);
                    boolean taintResult = taintManager.taint(className, classPathForSoot);
                    if (taintResult) {
                        RISK_SCORE = 5;
                        System.out.println("TAINT RESULT: CLASS IS MALICIOUS");
                    }
                    if (RISK_SCORE < 5) {
                        String classOutputPath;
                        classOutputPath = (classIsJSP) ? getOutputPath(className, JSP_FOLDER) : getOutputPath(className, DUMP_DIR);
                        System.out.println("classOutputPath: " + classOutputPath);
                        String decompiledClass = decompileClass(classOutputPath, null, false);
//                            System.out.println(decompiledClass);
//                            System.out.println("========================================= AFTER +++=++++++++++++++++++++");
//                            decompiledClass = removeCommentOfDecompileClass(decompiledClass);
                        Map<String, List<String>> filterResult = filterBlackList(decompiledClass, className, true);
                        RISK_SCORE += Integer.valueOf(filterResult.get("RiskScore").get(0));
                    }
                    handleResult(className, RISK_SCORE, null);
                    // Ask the user for input to delete the malicious class
                    askUser(inst,fullPathclassName,tempClass);

                    iterator.remove();
                    System.clearProperty(malKey);
                }

            Thread.sleep(1000);  // Replace with actual wait logic if applicable
        }
//            catch(InterruptedException e){
//                Thread.currentThread().interrupt();  // Preserve interrupt status
//            } catch(NotFoundException e){
//                throw new RuntimeException(e);
//            } catch(IOException e){
//                throw new RuntimeException(e);
//            }


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
/*
    public static void activeListening(Instrumentation inst) throws NotFoundException, IOException, CannotCompileException {
        System.out.println("========================= ACTIVE LISTENING ==============");
//        try {
            while (!Thread.currentThread().isInterrupted()) {
                final int[] RISK_SCORE = {0};
                ArrayList<String> delKey = new ArrayList<>();
                final Properties[] properties = {new Properties()};
                properties[0].putAll(System.getProperties());
                // Iterate over properties to find keys that start with "MAL"
//                System.out.println("System properties starting with 'MAL':");
                properties[0].entrySet().stream()
                        .filter(entry -> String.valueOf(entry.getKey()).startsWith("MAL"))
                        .forEach(entry -> {
                            try {
//                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                            String key = String.valueOf(entry.getKey());
                            System.out.println("key: " + key);
//                    System.out.println(key + ": " + entry.getValue());
                            if (key.startsWith("MAL")) {
                                String value = entry.getValue().toString();
                                System.out.println("======================================= FOUND MAL CLASS2");
                                System.out.println(key + " = " + value);
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
                                    System.out.println("Temp class Name: " + className);
//                            tempClass = getLoadedClassObjByCLassName(inst, className);
                                    if (!classIsJSP) {

                                            dumpClass(inst, className, DUMP_DIR);

                                    }


                                    className = className.substring(className.lastIndexOf(".") + 1);
                                    System.out.println("FOUND CLASS OBJECT FOR: " + className);
                                    boolean taintResult = taintManager.taint(className, classPathForSoot);
                                    if (taintResult) {
                                        RISK_SCORE[0] = 5;
                                        System.out.println("TAINT RESULT: CLASS IS MALICIOUS");
                                    }
                                    if (RISK_SCORE[0] < 5) {
                                        String classOutputPath;
                                        classOutputPath = (classIsJSP) ? getOutputPath(className, JSP_FOLDER) : getOutputPath(className, DUMP_DIR);
                                        System.out.println("classOutputPath: " + classOutputPath);
                                        String decompiledClass = decompileClass(classOutputPath, null, false);
//                            System.out.println(decompiledClass);
//                            System.out.println("========================================= AFTER +++=++++++++++++++++++++");
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
private static void askUser(Instrumentation inst , String className, Class<?> clazzObj) throws NotFoundException, IOException {
    if (!AUTO_DELETE){
        System.out.print("Do you want to delete this class by retransformation? (Y/N): ");
        String userInput = scanner.nextLine().trim().toUpperCase();

        if ("Y".equals(userInput)) {
            System.out.println("Retransforming and removing class: " + className);

        } else {
            System.out.println("Class " + className + " will not be removed.");
            return;
        }
    }
    dumpClass(inst,className,DUMP_DIR);
    List<Class<?>> classList = new ArrayList<>();
    classList.add(clazzObj);
    InstrumentationUtils.retransformClasses(inst,new DestroyTransformer(className), classList);
    System.out.println("Class " + className + " has been successfully removed.");
}

private static void handleResult(String className, int riskScore, Map<String, List<String>> filterResult) {
    System.out.println("============================================================  RESULT");
    System.out.println("Class: " + className + "riskScore: " + riskScore);
    if (riskScore <= 1) {
        System.out.println("Class is not malicious");
    } else if (riskScore >= 5) {
        System.out.println("Class is malicious");
    } else {
        System.out.println("Class is suspicious");

    }
//                            System.out.println("Class: " + className + "|| Point: " + riskPoint);
//        System.out.println("Matched Keywords:");
//        filterResult.forEach((key, value) -> {
//            if (value.isEmpty()) {
//                System.out.println(key + ": None");
//            } else {
//                System.out.println(key + ": " + value);
//            }
//        });
//        System.out.println("Analysis complete. Results written to log file.");
}

public static void createDumpFolder() {
    File dumpDir = new File(DUMP_DIR);
    if (!dumpDir.exists()) {
        boolean created = dumpDir.mkdirs();
        if (created) {
            System.out.println("Dump folder created at: " + DUMP_DIR);
        } else {
            System.out.println("Failed to create dump folder at: " + DUMP_DIR);
        }
    }
}

public static void printLoadedClass(Instrumentation inst) {
    System.out.println("All loaded classes: ");
    for (Class<?> clazz : inst.getAllLoadedClasses()) {
        if (clazz != null) {
            try {
                // Get class
//                    if (clazz.getPackage().toString().contains("org.example")) {
//                        String className = clazz.toString();
//                        System.out.println(className);
//                    }
                System.out.println("Class: " + clazz.getName() + ", Class Loader: " + clazz.getClassLoader());
            } catch (Exception e) {
                System.err.println("Error finding class: " + e.getMessage());
            }
//
        }
    }

}
}
