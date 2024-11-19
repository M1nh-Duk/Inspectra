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

        SootClass handlerMethodMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping");
        SootMethodRef registerMapping = new SootMethodRefImpl(handlerMethodMappingClass, "registerMapping", Collections.emptyList(), VoidType.v(), false);
        sources.add(registerMapping);

        SootClass servletRequestClass = Scene.v().getSootClass("javax.servlet.http.HttpServletRequest");
        SootMethodRef getParameter = new SootMethodRefImpl(servletRequestClass, "getParameter", Collections.singletonList(RefType.v("java.lang.String")), RefType.v("java.lang.String"), false);
        sources.add(getParameter);

        // AbstractUrlHandlerMapping: registerHandler
        SootClass urlHandlerMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping");
        SootMethodRef registerHandler = new SootMethodRefImpl(urlHandlerMappingClass, "registerHandler", Collections.emptyList(), VoidType.v(), false);
        sources.add(registerHandler);

        // Field: get
        SootClass fieldClass = Scene.v().getSootClass("java.lang.reflect.Field");
        SootMethodRef fieldGet = new SootMethodRefImpl(fieldClass, "get", Collections.singletonList(RefType.v("java.lang.Object")), RefType.v("java.lang.Object"), false);
        sources.add(fieldGet);

        // FilterDef: setFilterName
        SootClass filterDefClass = Scene.v().getSootClass("org.apache.tomcat.util.descriptor.web.FilterDef");
        SootMethodRef setFilterName = new SootMethodRefImpl(filterDefClass, "setFilterName", Collections.singletonList(RefType.v("java.lang.String")), VoidType.v(), false);
        sources.add(setFilterName);

        // StandardContext: addApplicationEventListener, addServletMappingDecoded
        SootClass standardContextClass = Scene.v().getSootClass("org.apache.catalina.core.StandardContext");
        SootMethodRef addApplicationEventListener = new SootMethodRefImpl(standardContextClass, "addApplicationEventListener", Arrays.asList(RefType.v("java.util.EventListener")), VoidType.v(), false);
        SootMethodRef addServletMappingDecoded = new SootMethodRefImpl(standardContextClass, "addServletMappingDecoded", Arrays.asList(RefType.v("java.lang.String"), RefType.v("java.lang.String")), VoidType.v(), false);
        sources.add(addApplicationEventListener);
        sources.add(addServletMappingDecoded);

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

    private Set<String> getResult(Object analysis) {
        List<SootMethod> entryPoints = getEntryPointMethods(); // Get all entry points
        Set<String> result = new HashSet<>();
        if (entryPoints.isEmpty()){
            System.out.println("entryPoints is empty");
        }
        for (SootMethod m : entryPoints) {
            System.out.println("Sootmethod: " + m.getName());
            // Skip methods that don't have an active body or are empty
            if (m == null || m.getActiveBody() == null || m.getActiveBody().getUnits().isEmpty()) {
                continue;
            }

            Map<DFF, Integer> res = null;
            if (analysis instanceof JimpleIFDSSolver) {
                JimpleIFDSSolver solver = (JimpleIFDSSolver) analysis;
                res = (Map<DFF, Integer>) solver.resultsAt(m.getActiveBody().getUnits().getLast());
            }

            // Add results from this method to the main result set
            if (res != null) {
                for (Map.Entry<DFF, Integer> e : res.entrySet()) {
                    result.add(e.getKey().toString());
                }
            }
        }

        // Print whether the set is empty or contains data
        System.out.println("================ TAINT RESULT");
        if (result.isEmpty()) {
            System.out.println("CLASS IS NOT MALICIOUS");
        } else {
            System.out.println("CLASS IS MALICIOUS");
        }
        System.out.print(result + "\n");
        return result;
    }

    public static byte[] readByteCode(String classFilePath) throws IOException {
        return Files.readAllBytes(Paths.get(classFilePath));
    }


    //    public static void main(String[] args) {
//        //Scanner scanner = new Scanner(System.in);
//        //System.out.println("Enter the target class name: ");
//        //String targetClassName = scanner.nextLine();
//
//        try {
//            org.IAP491G3.TaintAnalysis.TaintManager test = new org.IAP491G3.TaintAnalysis.TaintManager();
//            String className = "Evil";
//            String classPath = "D:\\Download\\Java 8\\TaintAnalysis-updated\\target\\test-classes\\target\\taint\\";
//
//            // Read bytecode
//            byte[] bytecode = readByteCode(classPath + className + ".class");
//
//            // Define the class from bytecode
//            Class<?> clazz = ByteCodeUtils.defineClassFromBytecode(bytecode);
//
//            // Parse the dynamically loaded class
//            JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = test.executeStaticAnalysis(clazz.getName());
//
//            // Get results from the analysis
//            Set<String> defaultIDEResult = test.getResult(analysis);
//
//        } catch (Exception e) {
//            System.err.println("Error during analysis: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    public void taint(Class<?> clazz) {
        System.out.println("================ TAINT METHOD EXECUTED !!!");
        System.out.println("Taint method received: " + clazz.getName());
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(clazz.getName());
        Set<String> defaultIDEResult = getResult(analysis);

    }
}