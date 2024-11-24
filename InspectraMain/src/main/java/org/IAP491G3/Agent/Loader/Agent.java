package org.IAP491G3.Agent.Loader;


import org.IAP491G3.Agent.Utils.LogUtils;
import org.IAP491G3.Agent.Utils.StringUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import static org.IAP491G3.Agent.Loader.Contraints.AGENT_NAME;
import static org.IAP491G3.Agent.Utils.InstrumentationUtils.invokeAgentCacheMethod;
import static org.IAP491G3.Agent.Utils.InstrumentationUtils.invokeAgentCacheMethodWithCast;
import static org.IAP491G3.Agent.Utils.PathUtils.getCurrentJarPath;


public class Agent {
    private static CustomClassLoader customClassLoader;
    private static Object AGENT_CACHE; // Singleton AgentCache

    private static synchronized void setAgentCache() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (customClassLoader != null) {
            Class<?> cacheClass = customClassLoader.loadClass("org.IAP491G3.Agent.Loader.AgentCache");
            Constructor<?> constructor = cacheClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            AGENT_CACHE = constructor.newInstance();
            StringUtils.println("AGENT_CACHE initialized successfully.");
        } else {
            System.err.println("customClassLoader is null. Cannot initialize AGENT_CACHE.");
        }
    }

    private static synchronized void setCustomClassLoader(URL jarFile) {
        if (customClassLoader == null) {
            customClassLoader = new CustomClassLoader(jarFile, Agent.class.getClassLoader());
        }
    }



    private static void detachAgent() throws Exception {
        synchronized (AGENT_CACHE) {
            Set<ClassFileTransformer> transformers = invokeAgentCacheMethodWithCast(AGENT_CACHE, "getTransformers", Set.class, false);
            Instrumentation instrumentation = invokeAgentCacheMethodWithCast(AGENT_CACHE, "getInstrumentation", Instrumentation.class, false);

            if (instrumentation != null) {

                Class<?>[] loadedClass = instrumentation.getAllLoadedClasses();
                Set<String> reTransformSet = new HashSet<String>();
                Set<String> retransformClass = invokeAgentCacheMethodWithCast(AGENT_CACHE, "getTransformedClass", Set.class, false);
                invokeAgentCacheMethodWithCast(AGENT_CACHE, "getModifiedClass", Set.class, false);
                reTransformSet.addAll(retransformClass);

                for (Iterator<ClassFileTransformer> iterator = transformers.iterator(); iterator.hasNext(); ) {
                    ClassFileTransformer transformer = iterator.next();
                    if (instrumentation.removeTransformer(transformer)){
                        StringUtils.println("Removing Transformer: " + transformer.getClass() + " Success");
                        iterator.remove();
                    }
                }

                for (Class<?> clazz : loadedClass) {
                    for (Iterator<String> iterator = reTransformSet.iterator(); iterator.hasNext(); ) {
                        String className = iterator.next();
                        if (clazz.getName().equals(className) && instrumentation.isModifiableClass(clazz)) {
                            try {
                                instrumentation.retransformClasses(clazz);
                                iterator.remove();
                                StringUtils.println("ReTransform " + clazz);
                            } catch (UnmodifiableClassException e) {
                                StringUtils.printAndLogErr(e);
                            }
                        }
                    }
                }
                invokeAgentCacheMethod(AGENT_CACHE, "clear", false);
            }

            if (customClassLoader != null && customClassLoader.closeClassLoader()) {
                StringUtils.println("Release SuAgent Resource Success");
                customClassLoader = null;
                AGENT_CACHE = null;
            }

            StringUtils.println("==================== DETACH SUCCESS !");
        }
    }

    private static void initiateAgent(final String arg, Instrumentation inst) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String[] args = arg != null ? arg.split("\\s+") : new String[0];
        try {
            StringUtils.println("Initiating agent...");
            if (customClassLoader == null) {
                File agentFile = new File(getCurrentJarPath());
                URL agentFileUrl = agentFile.toURI().toURL();
                inst.appendToBootstrapClassLoaderSearch(new JarFile(agentFile));
                setCustomClassLoader(agentFileUrl);
            }
            if (AGENT_CACHE == null) {
                StringUtils.println("AGENT_CACHE is null. Attempting to initialize...");
                setAgentCache();
                if (AGENT_CACHE == null) {
                    throw new IllegalStateException("Agent cache is null. Cannot initialize AGENT_CACHE.");
                }
            }
            synchronized (AGENT_CACHE) {
                try {
                    Instrumentation instTest = invokeAgentCacheMethodWithCast(AGENT_CACHE, "getInstrumentation", Instrumentation.class, false);
                    if (instTest!=null) {
                        StringUtils.println("Current cache inst: " + instTest);
                    }

                    if (args.length > 0) {
                        if ("detach".equalsIgnoreCase(args[0])) {
                            if (invokeAgentCacheMethodWithCast(AGENT_CACHE, "getInstrumentation", Instrumentation.class, false) == null) {
                                StringUtils.println(AGENT_NAME + "Not yet injected!");
                                return;
                            }
                            detachAgent();
                            return;
                        }
                        else if ("attach".equalsIgnoreCase(args[0]) && invokeAgentCacheMethodWithCast(AGENT_CACHE, "getInstrumentation", Instrumentation.class, false) != null) {
                            StringUtils.println(AGENT_NAME + "Already injected!");
                            return;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                invokeAgentCacheMethod(AGENT_CACHE, "setInstrumentation", true, inst);
//                StringUtils.println("Cache Instrumentation set successfully.");
//                    Instrumentation testInst = invokeAgentCacheMethodWithCast("getInstrumentation", Instrumentation.class);
//                    StringUtils.println();(testInst.toString());
                customClassLoader.loadAgent(new File(getCurrentJarPath()), arg, inst, AGENT_CACHE);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        StringUtils.println("\n=================\nAgentmain executed: Test Agent attached.");
        if (!inst.isRetransformClassesSupported()) {
            StringUtils.printAndLog("Class retransformation is not supported.");
            return;
        }
        try {
            initiateAgent(agentArgs, inst);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException e) {
            LogUtils.logit(e.getMessage());
            LogUtils.logit(e.getStackTrace().toString());
        }
//             printLoadedClass(inst);

    }

    public static void printLoadedClass(Instrumentation inst) {
        StringUtils.println("All loaded classes: ");
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz != null) {
                try {
                    // Get class
//                    if (clazz.getPackage().toString().contains("org.example")) {
//                        String className = clazz.toString();
//                        StringUtils.println();(className);
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