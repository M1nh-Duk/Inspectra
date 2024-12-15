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
        put("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping", "registerMapping");
        put("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping", "registerHandler");
        put("java.lang.reflect.Field", "get");
        put("org.apache.tomcat.util.descriptor.web.FilterDef", "setFilterClass");
        put("org.apache.catalina.core.StandardContext", Arrays.toString(new String[]{"addApplicationEventListener", "addServletMappingDecoded", "addServletMapping"}));
        put("org.apache.catalina.core.StandardWrapper", "setServletClass");
    }};

    public static ClassPool systemClassPool;
    private AgentCache agentCache;

    public MemoryTransformer() {
    }

    public void setAgentCache(AgentCache agentCache) {
        this.agentCache = agentCache;

    }

    //    ====================================================================================================================
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        boolean isSuspiciousClass = false;
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
          String convertedClassName = className.replace("/","."); // Remove the "/" in the className. Ex: org.IAP491G3.TaintAnalysis.Utils/StringUtils -> org.IAP491G3.TaintAnalysis.Utils.StringUtils;
        for (String malClassName : suspiciousClassAndMethod.keySet()) {
            if (convertedClassName.equals(malClassName)) {
                isSuspiciousClass = true;
                break;
            }
        }
        if (isSuspiciousClass) {
            try {

                ClassPool.getDefault().insertClassPath(new ClassClassPath(classBeingRedefined));
                ClassPool classPool = ClassPool.getDefault();
                classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    classPool.appendClassPath(new LoaderClassPath(contextClassLoader));
                }

                systemClassPool = classPool;
                String targetMethodName = suspiciousClassAndMethod.get(convertedClassName); // get the class only, not package
                return mainProbe(classPool, convertedClassName, targetMethodName);

            } catch (Exception e) {
                System.out.println("Exception in main probe: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return classfileBuffer;
    }

    private static byte[] mainProbe(ClassPool classPool, String targetClassName, String targetMethodName) throws Exception {
        if (classPool == null || targetClassName == null || targetMethodName == null) {
            throw new IllegalArgumentException("ClassPool, targetClassName, and targetMethodName must not be null.");
        }

        CtClass ctClazz;
        CtMethod ctMethod;
        Set<String> transformedClass = invokeAgentCacheMethodWithCast(Worker.workerAgentCache, "getTransformedClass", Set.class, false);
        transformedClass.add(targetClassName);

        List<String> targetMethodList = getTargetMethodList(targetMethodName); // Extracted method list creation

        try {
            ctClazz = classPool.get(targetClassName);
            // =========================================================================
            CtMethod isWhitelist = CtNewMethod.make(
                    "private static boolean isWhitelist(String className) {" +
                            // Check if the class belongs to a known web framework or server package
                            "if (className.startsWith(\"org.apache.jsp.uploads\"))"+
                            "{"+
                                "return false;"+
                            "}"+
                            "return className.startsWith(\"org.eclipse.jdt\") || className.startsWith(\"org.apache.\")" +
                            "|| className.startsWith(\"javax\") || className.startsWith(\"java.\") || className.startsWith(\"com.google.gson.internal\") ;" +
                            "}", ctClazz);
            ctClazz.addMethod(isWhitelist);

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
                            "sb.append(\",\");" +
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
                        ctMethod = declaredMethod;
                        StringUtils.println("Injecting into CtClass: " + ctClazz.getName() + ", Method: " + ctMethod.getName());
                        ctMethod.insertBefore(generateInsertedCode(ctMethod.getName()));
                    }
                }
            }

            byte[] byteCode = ctClazz.toBytecode();
            ctClazz.detach();
            return byteCode;
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find the class: " + targetClassName, e);
        } catch (CannotCompileException e) {
            throw new RuntimeException("Cannot compile the modified class: " + targetClassName, e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error occurred while processing class: " + targetClassName, e);
        }
    }


private static String generateInsertedCode(String methodName) {
        if (methodName.equals("get")){
            return "";
        }
    String customCode;
        if (methodName.equals("registerMapping") || methodName.equals("registerHandler")){
            customCode = "java.lang.System.setProperty(propertyName, \"\"+$2.getClass());";
        }
        else{
            customCode = ((methodName.equals("setFilterClass") || methodName.equals("setServletClass"))) ?
                    "java.lang.System.setProperty(propertyName, $1);":"java.lang.System.setProperty(propertyName, \"\"+$1.getClass());";
        }

    return "{ " +
            "try { " +

            "StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();" +
            "StackTraceElement maliciousClass = stackTrace[2];" +
            "String result = maliciousClass.getClassName();"+
            "if (!isWhitelist(maliciousClass.getClassName())){"+
            "if ( !isGeneratedJspClass(maliciousClass.getClassName()) && !isWhitelist(stackTrace[3].getClassName())) {"+
                    "result += \",\";"+
                    "int i = 3;"+
                    "while (i < stackTrace.length && !isWhitelist(stackTrace[i].getClassName())) {"+
                    "result += stackTrace[i].getClassName() + \",\";"+
                    "i++;"+
                    "}"+
            "}"+

            "String propertyName = \"MAL__" + methodName + "__\" + result +\"__\" + System.currentTimeMillis();" +
            customCode+
            "}"+
            "} catch (Exception e) { " +
            "System.out.println(\"Cause: \" + e.getCause().toString());" +
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