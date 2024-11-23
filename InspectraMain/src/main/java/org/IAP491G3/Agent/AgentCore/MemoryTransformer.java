package org.IAP491G3.Agent.AgentCore;

import org.IAP491G3.Agent.Loader.AgentCache;
import org.IAP491G3.Agent.Utils.StringUtils;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;

import static org.IAP491G3.Agent.Utils.InstrumentationUtils.invokeAgentCacheMethodWithCast;

public class MemoryTransformer implements ClassFileTransformer {
    public static final Map<String, String> suspiciousClassAndMethod = new HashMap<String, String>() {{
        put("AbstractHandlerMethodMapping", "registerMapping");
        put("AbstractUrlHandlerMapping", "registerHandler");
        put("java.lang.Field", "get");
        put("FilterDef", "setFilterName");
//        put("URLClassLoader", "defineClass");
        put("StandardContext", Arrays.toString(new String[]{"addApplicationEventListener", "addServletMappingDecoded", "addServletMapping"}));
//        put("Testing", "runCal");
//        put("ProcessBuilder", "start");
    }};
    public static List<ClassPool> systemClassPool = new ArrayList<>();
    private AgentCache agentCache;

    public MemoryTransformer() {
    }

    public void setAgentCache(AgentCache agentCache) {
        this.agentCache = agentCache;
        System.out.println("Transformer agent cache: "+ this.agentCache);

    }

    //    ====================================================================================================================
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        boolean isSuspiciousClass = false;

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
//        System.out.println("==================\nTRansformer executed");
//        System.out.println("ClassName: " + className);
//        System.out.println("Classloader: " + loader);

        String onlyClassName = className.substring(className.lastIndexOf("/") + 1);
        for (String malClassName : suspiciousClassAndMethod.keySet()) {
            if (onlyClassName.equals(malClassName)) {
                isSuspiciousClass = true;
                break;
            }
        }
        if (isSuspiciousClass) {
            try {

                System.out.println("Suspicious class: " + className);
                ClassPool.getDefault().insertClassPath(new ClassClassPath(classBeingRedefined));
                ClassPool classPool = ClassPool.getDefault();
                classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    classPool.appendClassPath(new LoaderClassPath(contextClassLoader));
                }

                systemClassPool.add(classPool);
                String targetMethodName = suspiciousClassAndMethod.get(onlyClassName); // get the class only, not package
                System.out.println("targetMethodName: " + targetMethodName);

                return mainProbe(classPool, className, targetMethodName);

            } catch (Exception e) {
                System.out.println("Exception in main probe: " + e.getMessage());
                e.printStackTrace();
            }
        }
//        System.out.println("END Transformer");
        return classfileBuffer;
    }

    private static byte[] mainProbe(ClassPool classPool, String targetClassName, String targetMethodName) throws Exception {
        if (classPool == null || targetClassName == null || targetMethodName == null) {
            throw new IllegalArgumentException("ClassPool, targetClassName, and targetMethodName must not be null.");
        }

        System.out.println("==================== Probe executed");
        CtClass ctClazz;
        CtMethod ctMethod;
        String fullPathClassName = targetClassName.replace("/", ".");  // Remove the "/" in the className. Ex: org.IAP491G3.TaintAnalysis.Utils/StringUtils -> org.IAP491G3.TaintAnalysis.Utils.StringUtils;
        Set<String> transformedClass = invokeAgentCacheMethodWithCast(Worker.workerAgentCache, "getTransformedClass", Set.class, false);
        transformedClass.add(fullPathClassName);
        String insertedCode = generateInsertedCode(); // Extracted code injection

        List<String> targetMethodList = getTargetMethodList(targetMethodName); // Extracted method list creation

        try {
            System.out.println("fullPathClassName: " + fullPathClassName);
            ctClazz = classPool.get(fullPathClassName);
            // =========================================================================
            CtMethod isFrameworkClassMethod = CtNewMethod.make(
                    "private static boolean isFrameworkClass(String className) {" +
                            // Check if the class belongs to a known web framework or server package
                            "return className.startsWith(\"org.apache.\") || className.startsWith(\"javax.servlet.\") || className.startsWith(\"java.\");" +
                            "}", ctClazz);
            ctClazz.addMethod(isFrameworkClassMethod);

            CtMethod isGeneratedJspClassMethod = CtNewMethod.make(
                    "private static boolean isGeneratedJspClass(String className) {" +
                            // Check if the class is a JSP-generated class (often contains 'jsp' or similar patterns)
                            "return className.contains(\"_jsp\") || className.contains(\"$jsp\");" +
                            "}", ctClazz);
            ctClazz.addMethod(isGeneratedJspClassMethod);
            CtMethod convertArrayMethod = CtNewMethod.make(
                    "private static String convertArray(Object[] array) {" +
                            "StringBuilder sb = new StringBuilder();" +
                            "sb.append(\"[\");" +
                            "for (int i = 0; i < array.length; i++) {" +
                            "sb.append(array[i] != null ? array[i].toString() : \"null\");" +
                            "if (i < array.length - 1) {" +
                            "sb.append(\", \");" +
                            "}" +
                            "}" +
                            "sb.append(\"]\");" +
                            "return sb.toString();" +
                            "}", ctClazz);
            ctClazz.addMethod(convertArrayMethod);
// =========================================================================
            for (CtMethod declaredMethod : ctClazz.getDeclaredMethods()) {
                for (String methodName : targetMethodList) {
                    if (declaredMethod.getName().equals(methodName)) {
                        ctMethod = declaredMethod; // Use declaredMethod instead of re-fetch
                        System.out.println("Injecting into CtClass: " + ctClazz.getName() + ", Method: " + ctMethod.getName());
                        ctMethod.insertAfter(insertedCode);
                    }
                }
            }

            byte[] byteCode = ctClazz.toBytecode();
            ctClazz.detach();
            return byteCode;
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find the class: " + fullPathClassName, e);
        } catch (CannotCompileException e) {
            throw new RuntimeException("Cannot compile the modified class: " + fullPathClassName, e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error occurred while processing class: " + fullPathClassName, e);
        }
    }

//    private static String generateInsertedCode() {
//        return "{ " +
//                "try { " +
//                "   System.out.println(\"=============== PROBE INJECT CODE EXECUTED\"); " +
////                "System.out.println(Thread.currentThread().getContextClassLoader());" +
//
//                    "System.out.println(\"Stack trace: \"); " +
//                    "StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();" +
//                    "StackTraceElement maliciousClass = null;" +
//                    "for (int i = 2; i < stackTrace.length; i++) {" +
//                        "StackTraceElement currentElement = stackTrace[i];" +
//                        "String currentClassName = currentElement.getClassName();" +
//                        "System.out.println(currentElement);" +
//                        // Check if the current element is a JSP-generated class
//                        "if (isGeneratedJspClass(currentClassName)) {" +
//                            "System.out.println(\"JSP-generated method (potential malicious upload): \" + currentElement.getClassName() + \".\" + currentElement.getMethodName());" +
//                            "maliciousClass = currentElement.getClassName();" +
//                            "break;" +
//                        "}" +
//
//                        // If current class is not Java core, keep checking until finding a framework class, set the developer class
//                            // If the i +1 class is not framework class, jump into this if case
//                        "if (i + 1 < stackTrace.length && !isFrameworkClass(stackTrace[i + 1].getClassName())) {" +
//                            // Continue until finding a framework class
//                            "int j = i;" +
//                            "while (j < stackTrace.length && !isFrameworkClass(stackTrace[j].getClassName())) {" +
//                                "j++;" +
//                            "}" +
//                            // Set the developer class as the current position - 1 and the previous one is malicious
//                            "if (j < stackTrace.length && isFrameworkClass(stackTrace[j].getClassName())) {" +
//                                "System.out.println(\"Closest developer method: \" + stackTrace[j - 2].getClassName() + \".\" + stackTrace[j - 2].getMethodName());" +
//                                "maliciousClass = stackTrace[j - 2].getClassName();" +
//                            "}" +
//                            "else if (j < stackTrace.length && isFrameworkClass(stackTrace[j].getClassName()) && !isFrameworkClass(stackTrace[j+1].getClassName())) {\n" +
//                                " j+=1;" +
//                                " continue;" +
//                            "}" +
//                        "}" +
////                        "else if () {\n" +
////                            " j+=1;" +
////                            " continue;" +
////                        "}" +
//                    "}" +
//                    "if (maliciousClass == null){" +
//                        " System.out.println(\"maliciousClass is NULL\"); " +
//                    "}" +
//
//                    "System.out.println(\"DETECTED MALICIOUS CLASS: \" + maliciousClass); " +
//                    "String propertyName = \"MAL_\" + maliciousClass + \"_\" + System.currentTimeMillis();" +
//                    "java.lang.System.setProperty(propertyName, \"\"+ maliciousClass); " +
//                    "System.out.println(\"Set system property successfully: \" + propertyName + \":\" + System.getProperty(propertyName)); " +
//                    "System.out.println(\"Array: \" + convertArray($args)); " +
//                    "System.out.println(\"END OF PROBE INJECT CODE\"); " +
//                "} catch (Exception e) { " +
//                    "e.printStackTrace();" +
//                "} " +
//                "}";
//    }
private static String generateInsertedCode() {
    return "{ " +
            "try { " +
            "   System.out.println(\"=============== PROBE INJECT CODE EXECUTED\"); " +
//                "System.out.println(Thread.currentThread().getContextClassLoader());" +

            "StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();" +
            "System.out.println(\"Stack trace: \"); " +
//            "for (int i = 0; i < stackTrace.length; i++) {" +
//            "StackTraceElement currentElement = stackTrace[i];" +
//            "String currentClassName = currentElement.getClassName();" +
//            "System.out.println(currentElement);" +
//            "}"+
            "StackTraceElement maliciousClass = stackTrace[2];" +
            "String result = maliciousClass.getClassName();"+
                "if (!isFrameworkClass(maliciousClass.getClassName()) && !isGeneratedJspClass(maliciousClass.getClassName()) && !isFrameworkClass(stackTrace[3].getClassName())) {"+
                    "result += \",\";"+
                    "int i = 3;"+
                    "while (i < stackTrace.length && !isFrameworkClass(stackTrace[i].getClassName())) {"+
                    "result += stackTrace[i].getClassName() + \",\";"+
                    "i++;"+
                    "}"+
                "}"+
            "System.out.println(\"DETECTED MALICIOUS CLASS: \" + maliciousClass); " +
            "String propertyName = \"MAL__\" + result + \"__\" + System.currentTimeMillis();" +
            "java.lang.System.setProperty(propertyName, convertArray($args)); " +
            "System.out.println(\"Set system property successfully: \" + propertyName + \":\" + java.lang.System.getProperty(propertyName)); " +
            "System.out.println(\"END OF PROBE INJECT CODE\"); " +
            "} catch (Exception e) { " +
            "e.printStackTrace();" +
            "} " +
            "}";
}
    private static List<String> getTargetMethodList(String targetMethodName) {
        List<String> targetMethodList = new ArrayList<>();
        if (targetMethodName.contains("[") && targetMethodName.contains(",")) {
            String[] targetMethodString = StringUtils.convertToStringArray(targetMethodName);
            targetMethodList.addAll(Arrays.asList(targetMethodString));
        } else {
            targetMethodList.add(targetMethodName);
        }
        return targetMethodList;
    }



}