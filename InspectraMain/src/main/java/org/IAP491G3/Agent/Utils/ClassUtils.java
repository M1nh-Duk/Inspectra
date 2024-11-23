package org.IAP491G3.Agent.Utils;


import javassist.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.*;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import static org.IAP491G3.Agent.AgentCore.MemoryTransformer.systemClassPool;
import static org.IAP491G3.Agent.Utils.StringUtils.getOutputPath;


public class ClassUtils {


    /*
 Ex:    Full path: org.apache.tomcat.util.descriptor.web.FilterDef
        Simple class name: FilterDef
        Class loader: java.net.URLClassLoader@3cef309d
     */
    public static Class<?> getLoadedClassObjByCLassName(Instrumentation inst, String className) {
        className = className.toLowerCase();
        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        for (Class<?> clazz : loadedClasses) {
            String clazzNameFull = clazz.getName().toLowerCase();
            if (clazzNameFull.endsWith(className) && !clazzNameFull.contains("$")) {
//                System.out.println("Method getLoadedClassObj successfully, Class: " + clazz.getName());
                return clazz;
            }
        }
        return null;
    }

    public static Class<?> getLoadedClassObjByFullPath(Instrumentation inst, String className) {
        className = className.toLowerCase();
        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        for (Class<?> clazz : loadedClasses) {
            String clazzNameFull = clazz.getName().toLowerCase();
            if (clazzNameFull.equals(className) && !clazzNameFull.contains("$")) {
                System.out.println("Method getLoadedClassObj successfully, Class: " + clazz.getName());
                return clazz;
            }
        }
        return null;
    }

    public static boolean checkLoaded(Instrumentation inst, String className) {
        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        for (Class<?> clazz : loadedClasses) {
            String clazzNameFull = clazz.getName();
            if (clazzNameFull.contains(className)) {
                return true;
            }
        }

        return false;
    }

    public static void saveBytecodeToFile(CtClass ctClass, String className, String folder) throws IOException {
        String outputPath = getOutputPath(className,folder);
        File outputFile = new File(outputPath);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(ctClass.toBytecode());
            ctClass.detach();
            System.out.println("Bytecode saved to: " + outputFile.getAbsolutePath());
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readByteCodeFromPath(String path) throws IOException {
        File classFile = new File(path);
        if (classFile.exists()) {
            try {
                return Files.readAllBytes(classFile.toPath());
            } catch (IOException e) {
                System.out.println("Error reading class file: " + e.getMessage());
            }
        } else {
            System.out.println("Class file does not exist: " + path);
        }

        return null; // Return null if the file does not exist or an error occurs
    }

    public static void dumpClass(Instrumentation inst, String className, String folder) throws NotFoundException, IOException {
        ClassPool classPool = systemClassPool.get(0);
        Class<?> classObj = getLoadedClassObjByFullPath(inst, className);
//        System.out.println("classObj test: " + classObj.getName());
        classPool.insertClassPath(new ClassClassPath(classObj));
        System.out.println("dumpClass ClassNAme: " + className);
        CtClass ctClass = classPool.get(className);
        System.out.println("Get successfully: " + ctClass.getName());
        saveBytecodeToFile(ctClass, className,folder);
    }

    public static String decompileClass(String classFilePath, String methodName, boolean hideUnicode) {
        final StringBuilder result = new StringBuilder(8192);

        OutputSinkFactory mySink = new OutputSinkFactory() {
            @Override
            public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
                return Arrays.asList(SinkClass.STRING, SinkClass.DECOMPILED, SinkClass.DECOMPILED_MULTIVER,
                        SinkClass.EXCEPTION_MESSAGE);
            }

            @Override
            public <T> Sink<T> getSink(final SinkType sinkType, SinkClass sinkClass) {
                return new Sink<T>() {
                    @Override
                    public void write(T sinkable) {
                        // skip message like: Analysing type demo.MathGame
                        if (sinkType == SinkType.PROGRESS) {
                            return;
                        }
                        result.append(sinkable);
                    }
                };
            }
        };

        HashMap<String, String> options = new HashMap<String, String>();
        /**
         * @see org.benf.cfr.reader.util.MiscConstants.Version.getVersion() Currently,
         *      the cfr version is wrong. so disable show cfr version.
         */
        options.put("showversion", "false");
        options.put("hideutf", String.valueOf(hideUnicode));
        if (!StringUtils.isBlank(methodName)) {
            options.put("methodname", methodName);
        }

        CfrDriver driver = new CfrDriver.Builder().withOptions(options).withOutputSink(mySink).build();
        List<String> toAnalyse = new ArrayList<String>();
        toAnalyse.add(classFilePath);
        driver.analyse(toAnalyse);

        return result.toString();
    }

}