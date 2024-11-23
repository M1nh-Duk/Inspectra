package org.IAP491G3.Agent.Loader;


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
import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import static org.IAP491G3.Agent.Loader.AgentAttacher.getAgentFileUrl;
import static org.IAP491G3.Agent.Loader.Contraints.AGENT_NAME;
import static org.IAP491G3.Agent.Utils.InstrumentationUtils.invokeAgentCacheMethod;
import static org.IAP491G3.Agent.Utils.InstrumentationUtils.invokeAgentCacheMethodWithCast;


public class Agent {
    private static CustomClassLoader customClassLoader;
    private static Object AGENT_CACHE; // Singleton AgentCache

    private static synchronized void setAgentCache() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (customClassLoader != null) {
            Class<?> cacheClass = customClassLoader.loadClass("org.IAP491G3.Agent.Loader.AgentCache");
            Constructor<?> constructor = cacheClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            AGENT_CACHE = constructor.newInstance();
            System.out.println("AGENT_CACHE initialized successfully.");
        } else {
            System.err.println("customClassLoader is null. Cannot initialize AGENT_CACHE.");
        }
    }

    private static synchronized void setCustomClassLoader(URL jarFile) {
        if (customClassLoader == null) {
            customClassLoader = new CustomClassLoader(jarFile, Agent.class.getClassLoader());
        }
    }


    public static File getAgentFile() throws MalformedURLException {
        return new File(getAgentFileUrl().getFile());
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
                        System.out.println("Removing Transformer: " + transformer.getClass() + " Success");
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
                                System.out.println("ReTransform " + clazz);
                            } catch (UnmodifiableClassException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                invokeAgentCacheMethod(AGENT_CACHE, "clear", false);
            }

            if (customClassLoader != null && customClassLoader.closeClassLoader()) {
                System.out.println("Release SuAgent Resource Success");
                customClassLoader = null;
                AGENT_CACHE = null;
            }

            System.out.println("==================== DETACH SUCCESS !");
        }
    }

    private static void initiateAgent(final String arg, Instrumentation inst) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String[] args = arg != null ? arg.split("\\s+") : new String[0];

//        try {
////
////                File loaderFile = getAgentFile();
////                File agentFile = getScannerJarFileUrl(loaderFile);
////                URL agentFileUrl = agentFile.toURI().toURL();
//            File agentFile = getAgentFile();
//            URL agentFileUrl = agentFile.toURI().toURL();
//
////                agentFileUrl= new URL(tempURL);
//            System.out.println("agentFile: " + agentFile);
//            System.out.println("getAgentFileUrl(): " + agentFileUrl);
//
////                System.out.println("agentFileUrl: " + agentFileUrl);
//            setCustomClassLoader(agentFileUrl);
//            setAgentCache();
//            synchronized (AGENT_CACHE) {
//                try {
//                    if (args.length > 0) {
//                        if ("detach".equalsIgnoreCase(args[0])) {
//                            detachAgent();
//                            return;
//                        } else if ("attach".equalsIgnoreCase(args[0]) && AGENT_CACHE.getInstrumentation() != null) {
//                            StringUtils.println(AGENT_NAME + "Already injected!");
//                            return;
//                        }
//                    }
//                    AGENT_CACHE.setInstrumentation(inst);
//
//                    inst.appendToBootstrapClassLoaderSearch(new JarFile(agentFile));
//
//                    customClassLoader.loadAgent(agentFile, arg, inst, AGENT_CACHE);
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                }
//            }
//        } catch (MalformedURLException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
//                 InstantiationException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
        // ================================================================


//        synchronized (Agent.class) {
        try {
            // Obtain the MBean server
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

            // Query for all StandardContext objects
            Set<ObjectName> contexts = mbs.queryNames(new ObjectName("Catalina:type=Context,*"), null);

            for (ObjectName context : contexts) {
                // Perform operations with the StandardContext
                String contextPath = context.getKeyProperty("path");
                System.out.println("Found context with path: " + contextPath);

                // You can cast here if you are sure of the context type
                // StandardContext standardContext = (StandardContext) someMethodToGetStandardContext();

                // Example: Do something with the context
                // ...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Initiating agent...");
            if (customClassLoader == null) {
                File agentFile = getAgentFile();
                URL agentFileUrl = agentFile.toURI().toURL();
                inst.appendToBootstrapClassLoaderSearch(new JarFile(agentFile));

                setCustomClassLoader(agentFileUrl);
            }
            if (AGENT_CACHE == null) {
                System.out.println("AGENT_CACHE is null. Attempting to initialize...");
                setAgentCache();
                if (AGENT_CACHE == null) {
                    throw new IllegalStateException("Failed to initialize AGENT_CACHE");
                }
            }
            synchronized (AGENT_CACHE) {
                try {
                    Instrumentation instTest = invokeAgentCacheMethodWithCast(AGENT_CACHE, "getInstrumentation", Instrumentation.class, false);
                    if (instTest!=null) {
                        System.out.println("Current cache inst: " + instTest);
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
                System.out.println("Attempting to initialize cache...");
                invokeAgentCacheMethod(AGENT_CACHE, "setInstrumentation", true, inst);
                System.out.println("Cache Instrumentation set successfully.");
//                    Instrumentation testInst = invokeAgentCacheMethodWithCast("getInstrumentation", Instrumentation.class);
//                    System.out.println(testInst.toString());
                customClassLoader.loadAgent(getAgentFile(), arg, inst, AGENT_CACHE);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        }

    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Premain executed: Test Agent attached.");
        if (!inst.isRetransformClassesSupported()) {
            System.out.println("Class retransformation is not supported.");
            return;
        }

        agentmain(agentArgs, inst);

    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("=================\nAgentmain executed: Test Agent attached.");
        System.out.println("This agent class loader: " + Agent.class.getClassLoader());
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("AgentCache: "+ AGENT_CACHE);
        System.out.println("Ctx Loader: " + contextLoader);
        if (!inst.isRetransformClassesSupported()) {
            System.out.println("Class retransformation is not supported.");
            return;
        }
        try {
            initiateAgent(agentArgs, inst);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
//             printLoadedClass(inst);

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