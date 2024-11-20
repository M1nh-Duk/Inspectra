package org.IAP491G3.TaintAnalysis.base;

import boomerang.scene.jimple.BoomerangPretransformer;
import soot.*;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public abstract class IFDSSetUp {

    protected static JimpleIFDSSolver<?, ?> solver = null;

    protected JimpleIFDSSolver<?, ?> executeStaticAnalysis(String targetTestClassName,ArrayList<String> userDefinedFolders ) {
        setupSoot(targetTestClassName,userDefinedFolders);
        registerSootTransformers();
        executeSootTransformers();
        if (solver == null) {
            throw new NullPointerException("Something went wrong solving the IFDS problem!");
        }
        return solver;
    }

    private void executeSootTransformers() {
        //Apply all necessary packs of soot. This will execute the respective Transformer
        PackManager.v().getPack("cg").apply();
        // Must have for Boomerang
        BoomerangPretransformer.v().reset();
        BoomerangPretransformer.v().apply();
        PackManager.v().getPack("wjtp").apply();
    }

    private void registerSootTransformers() {
        Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
        PackManager.v().getPack("wjtp").add(transform);
    }

    protected abstract Transformer createAnalysisTransformer();

    /*
     * This method provides the options to soot to analyse the respecive
     * classes.
     */
    private void setupSoot(String targetTestClassName,ArrayList<String> userDefinedFolders) {
        G.reset();
        System.out.println("========== SETTING UP SOOT !");
        String userdir = System.getProperty("user.dir");
        String javaHome = System.getProperty("java.home");

//        String sootCp = userdir + File.separator + "target" + File.separator + "test-classes" + File.pathSeparator + javaHome + File.separator + "lib" + File.separator + "rt.jar";
//        String sootCp = JSP_FOLDER  + DUMP_DIR + File.separator+ javaHome + File.separator + "lib" + File.separator + "rt.jar";
//        System.out.println("sootCp: " + sootCp);
//        Options.v().set_soot_classpath(sootCp);
        StringBuilder sootCpBuilder = new StringBuilder();
        for (String folder : userDefinedFolders) {
            sootCpBuilder.append(folder).append(File.pathSeparator);
        }
        sootCpBuilder.append(javaHome).append(File.separator).append("lib").append(File.separator).append("rt.jar");
        String sootCp = sootCpBuilder.toString();
        Options.v().set_soot_classpath(sootCp);
        System.out.println("sootCp: " + sootCp);
        // We want to perform a whole program, i.e. an interprocedural analysis.
        // We construct a basic CHA call graph for the program
        Options.v().set_whole_program(true);
        Options.v().setPhaseOption("cg.cha", "on");
        Options.v().setPhaseOption("cg", "all-reachable:true");

        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb.ls", "enabled:false");
        Options.v().set_prepend_classpath(false);

        Scene.v().addBasicClass("java.lang.StringBuilder");
        Scene.v().addBasicClass("java.lang.Object", SootClass.SIGNATURES);  // Obj/ect is fundamental
        Scene.v().addBasicClass("java.lang.ProcessBuilder", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.Runtime", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.reflect.Method", SootClass.SIGNATURES);
        SootClass c = Scene.v().forceResolve(targetTestClassName, SootClass.BODIES);
        if (c.isPhantom()) {
            System.out.println("The class is a phantom: " + c.getName());
        } else {
            System.out.println("Successfully converted to SootClass: " + c.getName());

            // Example: Print out all methods in the SootClass
            c.getMethods().forEach(method -> {
                System.out.println("Method: " + method.getName());
            });

            System.out.println("========== forceResolve class success with class: " + targetTestClassName);

            if (c != null) {
                c.setApplicationClass();
            }
            Scene.v().loadNecessaryClasses();
            System.out.println("========== DONE SET UP SOOT !");
        }
    }

    protected List<SootMethod> getEntryPointMethods() {
        System.out.println("================= Method getEntryPointMethods executed !!!");
        List<SootMethod> entryPoints = new ArrayList<>();
        for (SootClass c : Scene.v().getApplicationClasses()) {
            System.out.println("Detect sootclass: " + c.getName());
            if (c.getMethods().isEmpty()) {
                System.out.println("c.getMethods is null");
            }
            int methodCount = c.getMethodCount();
            System.out.println("Method count for class " + c.getName() + ": " + methodCount);

            for (SootMethod m : c.getMethods()) {
                System.out.println("SootMethod: " + m.getName());
                if (m.hasActiveBody()) {
                    System.out.println("M has body!");
                    // Add all methods with active bodies as entry points
                    entryPoints.add(m);
                }
            }
        }
        return entryPoints;
    }

}