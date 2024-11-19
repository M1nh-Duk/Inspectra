package org.IAP491G3.Agent.Utils;


import javassist.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.List;

import static org.IAP491G3.Agent.AgentCore.MemoryTransformer.systemClassPool;


public class ClassUtils {
    public static void retransformClasses(Instrumentation inst, ClassFileTransformer transformer,
                                          List<Class<?>> classes) {
        try {
            inst.addTransformer(transformer, true);

            for (Class<?> clazz : classes) {
                try {
                    inst.retransformClasses(clazz);
                    System.out.println("Retransform class successfully, Class: " + clazz.getName());

                } catch (Throwable e) {
                    System.out.println("====================\nRetransform error: "+e.getMessage());
                    System.out.println("Cause: "+e.getCause());
                    System.out.println("retransformClasses class error, name: " + clazz.getName());
                }
            }
        } finally {
            inst.removeTransformer(transformer);
        }
    }

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
    public static void saveBytecodeToFile(CtClass ctClass, String className) throws IOException {
        String filePath = "D:\\Download\\testProject\\Inspectra_JDK8\\Inspectra\\out\\" + className.replace('.', '_') + ".class";
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

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
    public static void dumpClass(Instrumentation inst, String className) throws NotFoundException, IOException {
        ClassPool classPool = systemClassPool.get(0);
        Class<?> classObj = getLoadedClassObjByFullPath(inst, className);
//        System.out.println("classObj test: " + classObj.getName());
        classPool.insertClassPath(new ClassClassPath(classObj));
        CtClass ctClass = classPool.get(className);
        System.out.println("Get successfully: " + ctClass.getName());
        saveBytecodeToFile(ctClass, className);

    }

    }