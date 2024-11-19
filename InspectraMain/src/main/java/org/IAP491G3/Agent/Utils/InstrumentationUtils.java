package org.IAP491G3.Agent.Utils;

import java.lang.reflect.Method;

public class InstrumentationUtils {
    public static Object invokeAgentCacheMethod(Object AGENT_CACHE, String methodName, Boolean hasArgument, Object... args) throws Exception {
        if (AGENT_CACHE == null) {
            throw new IllegalStateException("AgentCache is not initialized");
        }
        Class<?> cacheClass = AGENT_CACHE.getClass();
//        System.out.println("AgentCache class: " + cacheClass.getName());
//        System.out.println("args.length: " + args.length);
//        for (int i = 0; i < args.length; i++) {
//            System.out.println("Arg: " + args[i]);
//        }
        Method method = null;
        for (Method m : cacheClass.getDeclaredMethods()) {
//            System.out.println("MEthod - " + m.getName());
            if (m.getName().equals(methodName)) {
                if (m.getParameterCount() == args.length && hasArgument) {
                    boolean matches = true;
                    Class<?>[] paramTypes = m.getParameterTypes();
//                    System.out.println("Argument: ");
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

        System.out.println("Found method: " + method);
        method.setAccessible(true);
        if (hasArgument) {
            return method.invoke(AGENT_CACHE, args);

        }
        return method.invoke(AGENT_CACHE);

    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeAgentCacheMethodWithCast(Object AGENT_CACHE, String methodName, Class<T> returnType, Boolean hasArgument, Object... args) throws Exception {
        System.out.println("Attempting to invoke method: " + methodName);
//        System.out.println("AGENT_CACHE is " + (AGENT_CACHE == null ? "null" : "not null"));

        if (AGENT_CACHE == null) {
            throw new IllegalStateException("AGENT_CACHE is null");
        }

        Object result = invokeAgentCacheMethod(AGENT_CACHE, methodName, hasArgument, args);
        System.out.println("Method " + methodName + " returned: " + (result == null ? "null" : result.toString()));

        if (result == null) {
            return null;
        }
        if (returnType.isInstance(result)) {
            return (T) result;
        }
        throw new ClassCastException("Cannot cast " + result.getClass() + " to " + returnType);
    }


}