package org.IAP491G3.TaintAnalysis.Utils;

import org.objectweb.asm.ClassReader;
import java.lang.reflect.Method;

public class ByteCodeUtils {
    public static Class<?> defineClassFromBytecode(byte[] bytecode) throws Exception {
        String className = getClassNameFromBytecode(bytecode);

        ClassLoader customClassLoader = new ClassLoader(ByteCodeUtils.class.getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                try {
                    // Use reflection to invoke defineClass (which is protected)
                    Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                    defineClassMethod.setAccessible(true);
                    return (Class<?>) defineClassMethod.invoke(this, name, bytecode, 0, bytecode.length);
                } catch (Exception e) {
                    throw new ClassNotFoundException("Failed to define class from bytecode", e);
                }
            }
        };

        // Define the class using the name extracted from bytecode
        return customClassLoader.loadClass(className);
    }

    private static String getClassNameFromBytecode(byte[] bytecode) throws Exception {
        ClassReader reader = new ClassReader(bytecode);
        return reader.getClassName().replace('/', '.');
    }
}