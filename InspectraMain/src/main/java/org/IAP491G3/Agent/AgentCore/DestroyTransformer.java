package org.IAP491G3.Agent.AgentCore;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;


public class DestroyTransformer implements ClassFileTransformer {
    String className;

    DestroyTransformer(String className) {
        this.className = className;
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.out.println("==================== Probe executed");
        if (!this.className.contains(className)) {
            return classfileBuffer;
        }

        try {
            System.out.println("Delete class: " + className);
            ClassPool.getDefault().insertClassPath(new ClassClassPath(classBeingRedefined));
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClazz;
            String fullPathClassName = className.replace("/", ".");  // Remove the "/" in the className. Ex: org.IAP491G3.TaintAnalysis.Utils/StringUtils -> org.IAP491G3.TaintAnalysis.Utils.StringUtils;
            ctClazz = classPool.get(fullPathClassName);
            // =========================================================================
            CtMethod[] methods = ctClazz.getDeclaredMethods();
            for (CtMethod method : methods) {
                ctClazz.removeMethod(method);
            }
            CtField[] fields = ctClazz.getDeclaredFields();
            for (CtField field : fields) {
                if (!field.getName().equals("__TRANSFORMED_BY_AGENT")) {
                    ctClazz.removeField(field);
                }
            }
            // **Add a Marker Field**
            try {
                // Check if the marker field already exists
                ctClazz.getDeclaredField("__TRANSFORMED_BY_AGENT");
                System.out.println("Class already transformed, skipping marker addition.");
            } catch (NotFoundException e) {
                // If the field doesn't exist, add it
                CtField markerField = new CtField(CtClass.booleanType, "__TRANSFORMED_BY_AGENT", ctClazz);
                markerField.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
                ctClazz.addField(markerField, CtField.Initializer.constant(true));
                System.out.println("Marker added to class.");
            }

            // **Remove All Constructors**
            CtConstructor[] constructors = ctClazz.getDeclaredConstructors();
            for (CtConstructor constructor : constructors) {
                ctClazz.removeConstructor(constructor);
            }

            // **Write the transformed class back into the application**
            byte[] bytecode = ctClazz.toBytecode();
            System.out.println("Class successfully neutralized and marked.");
            ctClazz.detach();
            return bytecode;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
