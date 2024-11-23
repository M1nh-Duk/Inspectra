package org.IAP491G3.Agent.Loader;


import org.IAP491G3.Agent.Utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import static org.IAP491G3.Agent.Loader.Contraints.JAVA_INTERNAL_PACKAGES;


public class CustomClassLoader extends URLClassLoader {

    private String args;

    private Instrumentation instrumentation;

    private File agentFile;
    private Class<?> workerClass;

    public CustomClassLoader(final URL url, final ClassLoader classLoader) {
        super(new URL[]{url}, classLoader);

    }


    public String getArgs() {
        return args;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }


    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    public File getAgentFile() {
        return agentFile;
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);

        if (url != null) {
            return url;
        }

        return super.getResource(name);
    }


    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = findResources(name);

        if (urls != null) {
            return urls;
        }

        return super.getResources(name);
    }


    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        System.out.println("CustomLoader Attempting to load class: " + name);
        if (name.startsWith("org.apache.log4j")) {
            return getParent().getParent().loadClass(name);
        }

        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        if (name.matches(JAVA_INTERNAL_PACKAGES)) {
            return super.loadClass(name, resolve);
        }

        try {
            Class<?> clazz = findClass(name);

            if (resolve) {
                resolveClass(clazz);

            }

            return clazz;
        } catch (Exception e) {
            return super.loadClass(name, resolve);
        }
    }

    // load mới class agentCache, viết hàm update agentCache sau đó để match với agent cache hiện tại
    public void loadAgent(File agentFile, String args, Instrumentation inst, Object cache) throws Exception {
        this.args = args;
        this.instrumentation = inst;
        this.agentFile = agentFile;

        this.addURL(agentFile.toURL());
        this.workerClass = this.loadClass("org.IAP491G3.Agent.AgentCore.Worker");
        try {

            Method agentExecution = workerClass.getMethod(
                    "agentExecution", Instrumentation.class, cache.getClass()
            );
            agentExecution.invoke(null, instrumentation, cache);

        } catch (NoSuchMethodException e) {
            System.out.println("NoSuchMethodException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public boolean closeClassLoader() {
        try {
            System.out.println("Closing loader !");
            Method workerDeletingActiveThread = workerClass.getDeclaredMethod("deleteActiveThread");
            workerDeletingActiveThread.invoke(null);
            workerClass = null;
            Class<?> clazz = URLClassLoader.class;
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if ("close".equals(method.getName())) {
                    method.invoke(this);
                    return true;
                }
            }
            Field ucpField = clazz.getDeclaredField("ucp");
            ucpField.setAccessible(true);
            Object ucp = ucpField.get(this);

            Field loadersField = ucp.getClass().getDeclaredField("loaders");
            loadersField.setAccessible(true);
            List<?> loaders = (List<?>) loadersField.get(ucp);

            for (Object loader : loaders) {
                Class<?> jarLoaderClass = loader.getClass();
                Method method = jarLoaderClass.getDeclaredMethod("getJarFile");
                method.setAccessible(true);

                JarFile jarFile = (JarFile) method.invoke(loader);
                jarFile.close();

                StringUtils.println("Closed Jar: [" + jarFile.getName() + "]");
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
