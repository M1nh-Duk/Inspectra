package org.IAP491G3.Agent.AgentCore;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Objects;

import static org.IAP491G3.Agent.AgentCore.Worker.testClass;


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
            CtClass ctClazz;
            ctClazz = classPool.get(convertedClassName);

            // =========================================================================
            CtMethod[] methods = ctClazz.getDeclaredMethods();
            if (methods.length == 0) {
                System.out.println("Method is null");
            }
            ctClazz.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals("java.lang.Runtime") && m.getMethodName().equals("exec")) {
                        // Replace Runtime.exec() with logging and safe behavior
                        m.replace("{ System.out.println(\"Blocked Runtime.exec() call\"); return null; }");
                    } else if (m.getClassName().equals("java.lang.ProcessBuilder")) {
                        // Replace ProcessBuilder instantiation with logging
                        m.replace("{ System.out.println(\"Blocked ProcessBuilder instantiation\"); $_ = null; }");
                    }
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
//            CtField[] fields = ctClazz.getDeclaredFields();
//            for (CtField field : fields) {
//                if (!field.getName().equals("__TRANSFORMED_BY_AGENT")) {
//                    ctClazz.removeField(field);
//                }
//            }
            // **Add a Marker Field**
//            try {
//                // Check if the marker field already exists
//                ctClazz.getDeclaredField("__TRANSFORMED_BY_AGENT");
//                System.out.println("Class already transformed, skipping marker addition.");
//            } catch (NotFoundException e) {
//                // If the field doesn't exist, add it
//                CtField markerField = new CtField(CtClass.booleanType, "__TRANSFORMED_BY_AGENT", ctClazz);
//                markerField.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
//                ctClazz.addField(markerField, CtField.Initializer.constant(true));
//                System.out.println("Marker added to class.");
//            }

            // **Remove All Constructors**
//            CtConstructor[] constructors = ctClazz.getDeclaredConstructors();
//            for (CtConstructor constructor : constructors) {
//                ctClazz.removeConstructor(constructor);
//            }
//
//            // **Write the transformed class back into the application**
            byte[] bytecode = ctClazz.toBytecode();
            System.out.println("Class successfully neutralized and marked.");
            ctClazz.detach();
            return bytecode;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
