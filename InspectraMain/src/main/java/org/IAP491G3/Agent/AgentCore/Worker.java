package org.IAP491G3.Agent.AgentCore;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.*;

import static org.IAP491G3.Agent.AgentCore.MemoryTransformer.suspiciousClassAndMethod;
import static org.IAP491G3.Agent.Loader.Contraints.JSP_FOLDER;
import static org.IAP491G3.Agent.Loader.Contraints.OS_VERSION;
import static org.IAP491G3.Agent.Utils.ClassUtils.*;
import static org.IAP491G3.Agent.Utils.ClassUtils.getLoadedClassObjByCLassName;
import static org.IAP491G3.Agent.Utils.InstrumentationUtils.invokeAgentCacheMethodWithCast;
import static org.IAP491G3.Agent.Utils.StringUtils.extractClassName;
import static org.IAP491G3.Agent.Utils.StringUtils.printLogo;

import org.IAP491G3.Agent.Loader.AgentCache;
import org.IAP491G3.Agent.Loader.CustomClassLoader;
import org.IAP491G3.TaintAnalysis.TaintManager;

public class Worker {
    public static AgentCache workerAgentCache;
    protected static List<Class<?>> LOADED_CLASS = new ArrayList<>();
    private static Thread listeningThread;
    private static final TaintManager taintManager = new TaintManager();

    public static void agentExecution(Instrumentation inst, AgentCache agentCache) throws Exception {
        try {
            printLogo();
            System.out.println("========================= AGENT EXECUTION ==============");
//            System.out.println("11AgentCache class: " + agentCache.getClass().getName());
//            System.out.println("Worker classloader is " + Worker.class.getClassLoader());
            workerAgentCache = agentCache;

            handleLoadedClasses(inst);
            System.out.println("=========> Handled loaded class successfully !");
            System.out.println("========================= ADDING TRANSFORMER ==============");
            MemoryTransformer memoryTransformer = new MemoryTransformer();
            memoryTransformer.setAgentCache(agentCache);
            inst.addTransformer(memoryTransformer, true);
            System.out.println("=========> Add memoryTransformer successfully !");
            Set<ClassFileTransformer> test = invokeAgentCacheMethodWithCast(agentCache, "getTransformers", Set.class, false, inst);
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
//                if (loadedClassObj == null) {
//                    System.out.println("loadedClassObj is null");
//                }

//                System.out.println("Loaded class obj: " + loadedClassObj.getName());
                String tempClassName = null;
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
            retransformClasses(inst, new MemoryTransformer(), LOADED_CLASS);
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
            } catch (NotFoundException | IOException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        });
        listeningThread.setDaemon(true);  // Mark it as a daemon thread if you don't want it to prevent JVM from shutting down
        listeningThread.start();
    }

    public static void activeListening(Instrumentation inst) throws NotFoundException, IOException, CannotCompileException {
        System.out.println("========================= ACTIVE LISTENING ==============");
        try {
            while (!Thread.currentThread().isInterrupted()) {
//                Properties properties = new Properties(System.getProperties());
//                Properties properties = System.getProperties();
                Properties properties = new Properties();
                properties.putAll(System.getProperties());
                // Iterate over properties to find keys that start with "MAL"
//                System.out.println("System properties starting with 'MAL':");

                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String key = String.valueOf(entry.getKey());
//                    System.out.println(key + ": " + entry.getValue());
                    if (key.startsWith("MAL")) {
                        String value = entry.getValue().toString();
                        System.out.println("======================================= FOUND MAL CLASS2");
                        System.out.println(key + " = " + value);
                        Class<?> tempClass = null;
                        String tempClassName = null;
                        String[] parts = key.split("_");
                        if (key.contains("_jsp")) {
                            tempClassName = parts[1] + "_" + parts[2] + "_" + parts[3];  // MAL_org.apache.jsp.uploads._2addservlet_jsp_1731963948572
                        } else {
                            tempClassName = parts[1];                   // MAL_org.example.NormalServlet_1731963948572

                        }
//                                tempClass = getJspClass(inst, key);
//                                System.out.println("GOT CLASS: " + tempClass.getName());
//                            }
//                            else{
//                                getLoadedClassObjByCLassName(inst,key);
//                            }
//                        if (parts.length > 2) {
////            className = "_" + parts[parts.length - 3] + "_" + parts[parts.length - 2] + ".class";   // _2addFilter_jsp.class
//                            key = parts[2] + "_" + parts[3];   // _2addFilter_jsp.class
//                        }

//                        String className = extractClassName(key);
//                        System.out.println("Extracted className = " + className);

//                            tempClass = Class.forName(key);

                            if (tempClassName.contains("$")){
                                tempClassName= tempClassName.substring(0,tempClassName.indexOf("$"));
                            }
//                        dumpClass(inst, tempClassName);
                        System.out.println("Temp class Name: " + tempClassName);
                        tempClass = getLoadedClassObjByCLassName(inst, tempClassName);
                        System.out.println("FOUND CLASS OBJECT FOR: "+tempClass.getName());
                        taintManager.taint(tempClass);
//                        dumpClass(inst, className);
                        System.clearProperty(key);

                    }
                }
                properties = null;
                Thread.sleep(1000);  // Replace with actual wait logic if applicable


            }
        } catch (InterruptedException e) {
            // Handle interruption
            Thread.currentThread().interrupt();  // Preserve interrupt status
        }
//        catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
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
