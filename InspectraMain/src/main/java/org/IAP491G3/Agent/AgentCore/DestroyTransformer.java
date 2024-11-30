package org.IAP491G3.Agent.AgentCore;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;


public class DestroyTransformer implements ClassFileTransformer {
    private String malClassName;

//    DestroyTransformer(String className) {
//        this.malClassName = className;
//    }


    public DestroyTransformer(String malClassName) {
        this.malClassName = malClassName;
    }

    public void setMalClassName(String malClassName) {
        this.malClassName = malClassName;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
//        System.out.println("==================== DESTROY Probe executed");

        String convertedClassName = className.replace("/", ".");
//        System.out.println("MAL CLASS NAME: " + testClass);
//        System.out.println("Orignal Class: " + className);

        if (!this.malClassName.equals(convertedClassName)) {
            return classfileBuffer;
        }
        try {
            System.out.println("DESTROY TRANSFORM  class: " + className);
            ClassPool.getDefault().insertClassPath(new ClassClassPath(classBeingRedefined));
            ClassPool classPool = ClassPool.getDefault();
            System.out.println("Parent of current loader: " + loader.getParent().getParent().getParent().getParent());
            classPool.appendClassPath(new LoaderClassPath(loader.getParent().getParent().getParent().getParent()));
            CtClass ctClazz;
            ctClazz = classPool.get(convertedClassName);

            // =========================================================================
            CtMethod[] methods = ctClazz.getDeclaredMethods();
            if (methods.length == 0) {
                System.out.println("Method is null");
            }
            ctClazz.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException { //
                    if (m.getClassName().equals("java.lang.Runtime") && m.getMethodName().equals("exec") ) { //(java.lang.Process) null
                        // Replace Runtime.exec() with logging and safe behavior
                        System.out.println("Intercepting Runtime.exec() call!");
                        m.replace("{ System.out.println(\"Blocked Runtime.exec() call\"); $_ = (java.lang.Process) null; }");
                    } else if (m.getClassName().equals("java.lang.ProcessBuilder")) {
                        // Replace ProcessBuilder instantiation with logging
                        System.out.println("Intercepting ProcessBuilder call!");
                        m.replace("{ System.out.println(\"Blocked ProcessBuilder instantiation\"); $_ = null; }");
                    }
//                    else if (m.getClassName().equals("java.lang.reflect.Method") && m.getMethodName().equals("invoke") ) {
//                        // Replace Runtime.exec() with logging and safe behavior
//                        System.out.println("Intercepting reflection call!");
//                        m.replace("{ System.out.println(\"Blocked reflect call\"); $_ = (Object) null; }");
//                    }

                }
            });
//            for (CtMethod method : methods) {
//                if (method.getName().equals("_jspService")) {
//                    return classfileBuffer;
//                }
////                method.setBody("{ return ; }");
//                System.out.println("CTMETHOD: " + method.getName() + " SIGNATURE: " + method.getSignature() );
//                System.out.println("Return type : " + method.getReturnType() );
////                ctClazz.removeMethod(method);
////                if (method.getReturnType() == CtClass.voidType && !method.getName().equals("init") && !method.getName().equals("destroy")) {
////                    method.insertBefore("{ System.out.println(\"Method deleted\"); return;}");
////                }
//                if (Objects.equals(method.getName(), "doFilter")) {
//                    method.setBody("{ System.out.println(\"Method deleted\"); return;}");
//                }
////                if (method.getReturnType().isPrimitive()) {
////                    String returnStatement;
////                    if (method.getReturnType() == CtClass.intType) {
////                        returnStatement = "return 0;";
////                    } else if (method.getReturnType() == CtClass.booleanType) {
////                        returnStatement = "return false;";
////                    } else {
////                        returnStatement = "return 0;"; // Default for other primitive types
////                    }
////                    method.setBody("{" + returnStatement + "}");
////                } else {
////                    method.setBody("{ return; }");
////                }
//            }

            CtMethod isModifiedMethod = CtNewMethod.make(
                    "private static boolean isModifiedMethod () {" +
                            // Check if the class is a JSP-generated class (often contains 'jsp' or similar patterns)
                            "return true;" +
                            "}", ctClazz);
            ctClazz.addMethod(isModifiedMethod);

            byte[] bytecode = ctClazz.toBytecode();
            System.out.println("Class successfully neutralized and marked.");
            ctClazz.detach();
            return bytecode;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
