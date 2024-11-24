package org.IAP491G3.TaintAnalysis;

import org.IAP491G3.TaintAnalysis.analysis.IFDSTaintAnalysisProblem;
import org.IAP491G3.TaintAnalysis.analysis.data.DFF;
import heros.InterproceduralCFG;
import soot.*;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

import org.IAP491G3.TaintAnalysis.base.IFDSSetUp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaintManager extends IFDSSetUp {
    //    @Override
//    protected SceneTransformer createAnalysisTransformer() {
//
//        List<SootMethodRef> sources = new ArrayList<>();
//        List<SootMethodRef> sinks = new ArrayList<>();
//
//        SootClass handlerMethodMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping");
//        SootMethodRef registerMapping = new SootMethodRefImpl(handlerMethodMappingClass, "registerMapping", Collections.emptyList(), VoidType.v(), false);
//        sources.add(registerMapping);
//
//        SootClass servletRequestClass = Scene.v().getSootClass("javax.servlet.http.HttpServletRequest");
//        SootMethodRef getParameter = new SootMethodRefImpl(servletRequestClass, "getParameter", Collections.singletonList(RefType.v("java.lang.String")), RefType.v("java.lang.String"), false);
//        sources.add(getParameter);
//
//        // AbstractUrlHandlerMapping: registerHandler
//        SootClass urlHandlerMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping");
//        SootMethodRef registerHandler = new SootMethodRefImpl(urlHandlerMappingClass, "registerHandler", Collections.emptyList(), VoidType.v(), false);
//        sources.add(registerHandler);
//
//        // Field: get
//        SootClass fieldClass = Scene.v().getSootClass("java.lang.reflect.Field");
//        SootMethodRef fieldGet = new SootMethodRefImpl(fieldClass, "get", Collections.singletonList(RefType.v("java.lang.Object")), RefType.v("java.lang.Object"), false);
//        sources.add(fieldGet);
//
//        // FilterDef: setFilterName
//        SootClass filterDefClass = Scene.v().getSootClass("org.apache.tomcat.util.descriptor.web.FilterDef");
//        SootMethodRef setFilterName = new SootMethodRefImpl(filterDefClass, "setFilterName", Collections.singletonList(RefType.v("java.lang.String")), VoidType.v(), false);
//        sources.add(setFilterName);
//
//        // StandardContext: addApplicationEventListener, addServletMappingDecoded
//        SootClass standardContextClass = Scene.v().getSootClass("org.apache.catalina.core.StandardContext");
//        SootMethodRef addApplicationEventListener = new SootMethodRefImpl(standardContextClass, "addApplicationEventListener", Arrays.asList(RefType.v("java.util.EventListener")), VoidType.v(), false);
//        SootMethodRef addServletMappingDecoded = new SootMethodRefImpl(standardContextClass, "addServletMappingDecoded", Arrays.asList(RefType.v("java.lang.String"), RefType.v("java.lang.String")), VoidType.v(), false);
//        sources.add(addApplicationEventListener);
//        sources.add(addServletMappingDecoded);
//
//        SootClass processBuilderClass = Scene.v().getSootClass("java.lang.ProcessBuilder");
//        SootMethodRef processBuilderStart = new SootMethodRefImpl(processBuilderClass, "start", Collections.emptyList(), RefType.v("java.lang.Process"), true);
//        sinks.add(processBuilderStart);
//
//        SootClass runtimeClass = Scene.v().getSootClass("java.lang.Runtime");
//        SootMethodRef runtimeExecString = new SootMethodRefImpl(runtimeClass, "exec", Collections.singletonList(RefType.v("java.lang.String")), RefType.v("java.lang.Process"), true);
//        sinks.add(runtimeExecString);
//        SootMethodRef runtimeExecStringArray = new SootMethodRefImpl(runtimeClass, "exec", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)), RefType.v("java.lang.Process"), true);
//        sinks.add(runtimeExecStringArray);
//
//        SootClass methodClass = Scene.v().getSootClass("java.lang.reflect.Method");
//        SootMethodRef methodInvoke = new SootMethodRefImpl(methodClass, "invoke", Arrays.asList(RefType.v("java.lang.Object"), ArrayType.v(RefType.v("java.lang.Object"), 1)), RefType.v("java.lang.Object"), true);
//        sinks.add(methodInvoke);
//
//        return new SceneTransformer() {
//            @Override
//            protected void internalTransform(String phaseName, Map<String, String> options) {
//                JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(false);
//                IFDSTaintAnalysisProblem problem = new IFDSTaintAnalysisProblem(icfg, sources, sinks);
//                @SuppressWarnings({"rawtypes", "unchecked"})
//                JimpleIFDSSolver<?, ?> solver = new JimpleIFDSSolver<>(problem);
//                solver.solve();
//                IFDSSetUp.solver = solver;
//            }
//        };
//    }
    @Override
    protected Transformer createAnalysisTransformer() {

        List<SootMethodRef> sources = new ArrayList<>();
        List<SootMethodRef> sinks = new ArrayList<>();

//        SootClass handlerMethodMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping");
//        SootMethodRef registerMapping = new SootMethodRefImpl(handlerMethodMappingClass, "registerMapping", Collections.emptyList(), VoidType.v(), false);
//        sources.add(registerMapping);

        SootClass servletRequestClass = Scene.v().getSootClass("javax.servlet.http.HttpServletRequest");
        SootMethodRef getParameter = new SootMethodRefImpl(servletRequestClass, "getParameter", Collections.singletonList(RefType.v("java.lang.String")), RefType.v("java.lang.String"), false);
        sources.add(getParameter);

        // AbstractUrlHandlerMapping: registerHandler
//        SootClass urlHandlerMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping");
//        SootMethodRef registerHandler = new SootMethodRefImpl(urlHandlerMappingClass, "registerHandler", Collections.emptyList(), VoidType.v(), false);
//        sources.add(registerHandler);

        // Field: get
//        SootClass fieldClass = Scene.v().getSootClass("java.lang.reflect.Field");
//        SootMethodRef fieldGet = new SootMethodRefImpl(fieldClass, "get", Collections.singletonList(RefType.v("java.lang.Object")), RefType.v("java.lang.Object"), false);
//        sources.add(fieldGet);

        // FilterDef: setFilterName
//        SootClass filterDefClass = Scene.v().getSootClass("org.apache.tomcat.util.descriptor.web.FilterDef");
//        SootMethodRef setFilterName = new SootMethodRefImpl(filterDefClass, "setFilterName", Collections.singletonList(RefType.v("java.lang.String")), VoidType.v(), false);
//        sources.add(setFilterName);

        // StandardContext: addApplicationEventListener, addServletMappingDecoded
//        SootClass standardContextClass = Scene.v().getSootClass("org.apache.catalina.core.StandardContext");
//        SootMethodRef addApplicationEventListener = new SootMethodRefImpl(standardContextClass, "addApplicationEventListener", Arrays.asList(RefType.v("java.util.EventListener")), VoidType.v(), false);
//        SootMethodRef addServletMappingDecoded = new SootMethodRefImpl(standardContextClass, "addServletMappingDecoded", Arrays.asList(RefType.v("java.lang.String"), RefType.v("java.lang.String")), VoidType.v(), false);
//        sources.add(addApplicationEventListener);
//        sources.add(addServletMappingDecoded);

        SootClass processBuilderClass = Scene.v().getSootClass("java.lang.ProcessBuilder");
        SootMethodRef processBuilderStart = new SootMethodRefImpl(processBuilderClass, "start", Collections.emptyList(), RefType.v("java.lang.Process"), true);
        sinks.add(processBuilderStart);

        SootClass runtimeClass = Scene.v().getSootClass("java.lang.Runtime");
        SootMethodRef runtimeExecString = new SootMethodRefImpl(runtimeClass, "exec", Collections.singletonList(RefType.v("java.lang.String")), RefType.v("java.lang.Process"), true);
        sinks.add(runtimeExecString);
        SootMethodRef runtimeExecStringArray = new SootMethodRefImpl(runtimeClass, "exec", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)), RefType.v("java.lang.Process"), true);
        sinks.add(runtimeExecStringArray);

        SootClass methodClass = Scene.v().getSootClass("java.lang.reflect.Method");
        SootMethodRef methodInvoke = new SootMethodRefImpl(methodClass, "invoke", Arrays.asList(RefType.v("java.lang.Object"), ArrayType.v(RefType.v("java.lang.Object"), 1)), RefType.v("java.lang.Object"), true);
        sinks.add(methodInvoke);
        return new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map<String, String> options) {
                JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(false);
                IFDSTaintAnalysisProblem problem = new IFDSTaintAnalysisProblem(icfg, sources, sinks);
                @SuppressWarnings({"rawtypes", "unchecked"})
                JimpleIFDSSolver<?, ?> solver = new JimpleIFDSSolver<>(problem);
                solver.solve();
                org.IAP491G3.TaintAnalysis.base.IFDSSetUp.solver = solver;
            }
        };
//        return new SceneTransformer() {
//            @Override
//            protected void internalTransform(String phaseName, Map<String, String> options) {
//                JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(false);
//                IFDSTaintAnalysisProblem problem = new IFDSTaintAnalysisProblem(icfg, sources, sinks);
//                @SuppressWarnings({"rawtypes", "unchecked"})
//                JimpleIFDSSolver<?, ?> solver = new JimpleIFDSSolver<>(problem);
//                solver.solve();
//                IFDSSetUp.solver = solver;
//            }
//        };
    }
    private boolean getResult(Object analysis) {
        List<SootMethod> entryPoints = getEntryPointMethods(); // Get all entry points
        Set<String> result = new HashSet<>();
        boolean containsMaliciousIndicator = false; // Flag to check for malicious indicators

        if (entryPoints.isEmpty()) {
            System.out.println("entryPoints is empty");
        }

        for (SootMethod m : entryPoints) {
            if (m == null || !m.hasActiveBody()) {
                continue;
            }

            // Safely get the active body
            Body activeBody = m.getActiveBody();

            // Ensure the active body is not empty
            if (activeBody.getUnits().isEmpty()) {
                continue;
            }

            // Log method details and its statements
//            System.out.println("SootMethod: " + m.getSignature());
            for (Unit unit : activeBody.getUnits()) {
                String unitString = unit.toString();
//                System.out.println("UNIT STRING: " + unitString);
                // Check for sinks: ProcessBuilder.start, Runtime.exec (String), Runtime.exec (String[]), and Method.invoke
                if (unitString.contains("java.lang.ProcessBuilder") && unitString.contains("start")) {
                    containsMaliciousIndicator = true;
//                    System.out.println("Potential malicious indicator found in statement (ProcessBuilder.start): " + unitString);
                } else if (unitString.contains("java.lang.Runtime") && unitString.contains("exec")) {
                    if (unitString.contains("(java.lang.String)") || unitString.contains("(java.lang.String[])")) {
                        containsMaliciousIndicator = true;
//                        System.out.println("Potential malicious indicator found in statement (Runtime.exec): " + unitString);
                    }
                }
                else if (unitString.contains("java.lang.reflect.Method") && unitString.contains("invoke")) {
                    containsMaliciousIndicator = true;
//                    System.out.println("Potential malicious indicator found in statement (Method.invoke): " + unitString);
                }
                else if (unitString.contains("ClassLoader")) {
                    containsMaliciousIndicator = true;
//                    System.out.println("Potential malicious indicator found in statement (ClassLoader): " + unitString);
                }
            }

            Map<DFF, Integer> res = null;
            if (analysis instanceof JimpleIFDSSolver) {
                JimpleIFDSSolver solver = (JimpleIFDSSolver) analysis;
                res = (Map<DFF, Integer>) solver.resultsAt(activeBody.getUnits().getLast());
            }

            // Add results from this method to the main result set
            if (res != null) {
                for (Map.Entry<DFF, Integer> e : res.entrySet()) {
                    result.add(e.getKey().toString());
                }
            }
        }

//        System.out.println("================ TAINT RESULT");
//        if (containsMaliciousIndicator) {
//            System.out.println("CLASS IS MALICIOUS");
//        } else {
//            System.out.println("CLASS IS NOT MALICIOUS");
//        }
//
//        System.out.print(result + "\n");
        return containsMaliciousIndicator;
    }
//
//    private boolean getRsult(Object analysis) {
//        List<SootMethod> entryPoints = getEntryPointMethods(); // Get all entry points
//        Set<String> result = new HashSet<>();R
//        if (entryPoints.isEmpty()){
//            System.out.println("entryPoints is empty");
//        }
//        for (SootMethod m : entryPoints) {
////            System.out.println("Sootmethod: " + m.getName());
//            // Skip methods that don't have an active body or are empty
//            if (m == null || m.getActiveBody() == null || m.getActiveBody().getUnits().isEmpty()) {
//                continue;
//            }
//
//            Map<DFF, Integer> res = null;
//            if (analysis instanceof JimpleIFDSSolver) {
//                JimpleIFDSSolver solver = (JimpleIFDSSolver) analysis;
//                res = (Map<DFF, Integer>) solver.resultsAt(m.getActiveBody().getUnits().getLast());
//            }
//
//            // Add results from this method to the main result set
//            if (res != null) {
//                for (Map.Entry<DFF, Integer> e : res.entrySet()) {
//                    result.add(e.getKey().toString());
//                }
//            }
//        }
//
//        // Print whether the set is empty or contains data
////        System.out.println("================ TAINT RESULT");
//        if (result.isEmpty()) {
//            return false;
//            //System.out.println("CLASS IS NOT MALICIOUS");
//        } else {
//            return true;
////            System.out.println("CLASS IS MALICIOUS");
//        }
////        System.out.print(result + "\n");
////        return result;
//    }

    public static byte[] readByteCode(String classFilePath) throws IOException {
        return Files.readAllBytes(Paths.get(classFilePath));
    }

//    public boolean taint(String className, ArrayList<String> userDefinedFolders) {
//        System.out.println("================ TAINT METHOD EXECUTED !!!");
//        System.out.println("Taint method received: " + className);
//        boolean check = false;
////        AtomicReference<Set<String>> defaultIDEResult = new AtomicReference<>();
//        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(className, userDefinedFolders);
//        check = getRsult(analysis);
//        return check;
//    }


    public boolean taint(String className, ArrayList<String> userDefinedFolders) {
        System.out.println("TAINT METHOD EXECUTED !!!");
        System.out.println("Taint method received: " + className);
//        AtomicReference<Set<String>> defaultIDEResult = new AtomicReference<>();
        AtomicBoolean check = new AtomicBoolean(false);
        Thread analysisThread = new Thread(() -> {
            try {
                JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(className, userDefinedFolders);
                check.set(getResult(analysis));
            } finally {
                System.out.println("Taint analysis completed, thread shutting down.");
            }
        });
//        System.out.println("================ TAINT RESULT");

        analysisThread.start();

        try {
            analysisThread.join();
        } catch (InterruptedException e) {
            System.err.println("Analysis thread was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        return check.get();

    }
}