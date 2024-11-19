package org.IAP491G3.Agent.Loader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Agent cache class for managing instrumentation and agent-related data.
 * Implements a singleton pattern to ensure only one instance is used.
 */
public class AgentCache {

    // Static singleton instance
    private static volatile AgentCache instance;
    private static final Object lock = new Object();

    // Instrumentation instance for managing JVM interactions
    private Instrumentation instrumentation;

    // Synchronized sets for managing agent data
    private final Set<String> modifiedClass = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> transformedClass = Collections.synchronizedSet(new HashSet<>());
    private final Set<ClassFileTransformer> transformers = Collections.synchronizedSet(new HashSet<>());

    // Private constructor to enforce singleton pattern
    public AgentCache() {}

    /**
     * Singleton instance getter.
     * Ensures that only one instance of AgentCache is created (thread-safe).
     *
     * @return The singleton instance of AgentCache.
     */
    public static AgentCache getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new AgentCache();
                }
            }
        }
        return instance;
    }

    /**
     * Get the current instrumentation.
     *
     * @return The instrumentation instance.
     */
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    /**
     * Set the instrumentation instance if not already set.
     *
     * @param instrumentation The instrumentation instance to set.
     */
    public void setInstrumentation(Instrumentation instrumentation) {
        if (this.instrumentation == null) {
            this.instrumentation = instrumentation;
        }
    }

    /**
     * Get the set of modified classes.
     *
     * @return The set of modified classes.
     */
    public Set<String> getModifiedClass() {
        return modifiedClass;
    }

    /**
     * Get the set of retransformed classes.
     *
     * @return The set of retransformed classes.
     */
    public Set<String> getTransformedClass() {
        return transformedClass;
    }

    /**
     * Get the set of ClassFileTransformers.
     *
     * @return The set of ClassFileTransformers.
     */
    public Set<ClassFileTransformer> getTransformers() {
        return transformers;
    }

    /**
     * Clear all the cached data and instrumentation.
     * Used during detachment to clean up resources.
     */
    public void clear() {
        synchronized (lock) {
            instrumentation = null;
            modifiedClass.clear();
            transformedClass.clear();
            transformers.clear();
        }
    }

    /**
     * Set all modified classes. Useful for copying data from one cache instance to another.
     *
     * @param modifiedClasses The set of modified classes to set.
     */
    public void setModifiedClass(Set<String> modifiedClasses) {
        synchronized (lock) {
            this.modifiedClass.addAll(modifiedClasses);
        }
    }

    /**
     * Set all retransformed classes. Useful for copying data from one cache instance to another.
     *
     * @param reTransformClasses The set of retransformed classes to set.
     */
    public void setReTransformClass(Set<String> reTransformClasses) {
        synchronized (lock) {
            this.transformedClass.addAll(reTransformClasses);
        }
    }

    /**
     * Set all transformers. Useful for copying data from one cache instance to another.
     *
     * @param transformerSet The set of transformers to set.
     */
    public void setTransformers(Set<ClassFileTransformer> transformerSet) {
        synchronized (lock) {
            this.transformers.addAll(transformerSet);
        }
    }
}
