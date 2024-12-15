package org.IAP491G3.Agent.AgentCore;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.IAP491G3.Agent.Utils.StringUtils;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import static org.IAP491G3.Agent.AgentCore.MemoryTransformer.systemClassPool;


public class DestroyTransformer implements ClassFileTransformer {
    private String malClassName;


    public DestroyTransformer(String malClassName) {
        this.malClassName = malClassName;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        String convertedClassName = className.replace("/", ".");
        if (!this.malClassName.equals(convertedClassName)) {
            return classfileBuffer;
        }
        try {
            ClassPool.getDefault().insertClassPath(new ClassClassPath(classBeingRedefined));
            ClassPool classPool = ClassPool.getDefault();
            systemClassPool.insertClassPath(new ClassClassPath(classBeingRedefined));

//            ClassPool classPool = systemClassPool;
//            classPool.appendClassPath(new LoaderClassPath(loader.getParent().getParent().getParent().getParent()));
            CtClass ctClazz;
            ctClazz = classPool.get(convertedClassName);
            // =========================================================================
            CtMethod[] methods = ctClazz.getDeclaredMethods();
            ctClazz.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException { //
                    if (m.getClassName().equals("java.lang.Runtime") && m.getMethodName().equals("exec") ) {
                        // Replace Runtime.exec() with logging and safe behavior
                        StringUtils.println("Intercepting Runtime.exec() call!");
                        m.replace("{ System.out.println(\"Blocked Runtime.exec() call\"); $_ = (java.lang.Process) null; }");
                    } else if (m.getClassName().equals("java.lang.ProcessBuilder")) {
                        // Replace ProcessBuilder instantiation with logging
                        StringUtils.println("Intercepting ProcessBuilder call!");
                        m.replace("{ System.out.println(\"Blocked ProcessBuilder instantiation\"); $_ = null; }");
                    }
                    else if (m.getClassName().equals("java.lang.reflect.Method") && m.getMethodName().equals("invoke") ) {
                        // Replace Runtime.exec() with logging and safe behavior
                        StringUtils.println("Intercepting reflection call!");
                        m.replace("{ System.out.println(\"Blocked reflect call\"); $_ = (Object) null; }");
                    }

                }
            });

            CtMethod isModifiedMethod = CtNewMethod.make(
                    "private static boolean isModifiedMethod () {" +
                            // Check if the class is a JSP-generated class (often contains 'jsp' or similar patterns)
                            "return true;" +
                            "}", ctClazz);
            ctClazz.addMethod(isModifiedMethod);

            byte[] bytecode = ctClazz.toBytecode();
            StringUtils.println("Class successfully neutralized and marked.");
            ctClazz.detach();
            return bytecode;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
