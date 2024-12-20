package org.IAP491G3.Agent.Utils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.List;

public class InstrumentationUtils {
    public static Object invokeAgentCacheMethod(Object AGENT_CACHE, String methodName, Boolean hasArgument, Object... args) throws Exception {
        if (AGENT_CACHE == null) {
            throw new IllegalStateException("AgentCache is not initialized");
        }
        Class<?> cacheClass = AGENT_CACHE.getClass();
        Method method = null;
        for (Method m : cacheClass.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                if (m.getParameterCount() == args.length && hasArgument) {
                    boolean matches = true;
                    Class<?>[] paramTypes = m.getParameterTypes();
                    for (int i = 0; i < args.length; i++) {
                        System.out.println(args[i]);
                        if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        method = m;
                        break;
                    }
                } else if (!hasArgument) {
                    method = m;
                    break;
                } else {
                    throw new Exception("Possible mismatch number of arguments !");
                }
            }
        }

        if (method == null) {
            throw new NoSuchMethodException("Method " + methodName + " not found in AgentCache");
        }

        method.setAccessible(true);
        if (hasArgument) {
            return method.invoke(AGENT_CACHE, args);

        }
        return method.invoke(AGENT_CACHE);

    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeAgentCacheMethodWithCast(Object AGENT_CACHE, String methodName, Class<T> returnType, Boolean hasArgument, Object... args) throws Exception {


        if (AGENT_CACHE == null) {
            throw new IllegalStateException("AGENT_CACHE is null");
        }

        Object result = invokeAgentCacheMethod(AGENT_CACHE, methodName, hasArgument, args);

        if (result == null) {
            return null;
        }
        if (returnType.isInstance(result)) {
            return (T) result;
        }
        throw new ClassCastException("Cannot cast " + result.getClass() + " to " + returnType);
    }

    public static void retransformClasses(Instrumentation inst, ClassFileTransformer transformer,
                                          List<Class<?>> classes) throws UnmodifiableClassException, RuntimeException {
        inst.addTransformer(transformer, true);
        for (Class<?> clazz : classes) {
            if (!isClassAlreadyTransformed(clazz)) {
                inst.retransformClasses(clazz);
                StringUtils.println("Retransform class successfully, Class: " + clazz.getName());

            }
        }
        inst.removeTransformer(transformer);

    }

    public static boolean isClassAlreadyTransformed(Class<?> clazz) {
        try {
            // Check if the class has the marker field
            clazz.getDeclaredMethod("isModifiedMethod");

            return true;  // Marker exists, the class has been transformed
        } catch (NoSuchMethodException e) {
            return false;  // Marker doesn't exist, class needs transformation
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
