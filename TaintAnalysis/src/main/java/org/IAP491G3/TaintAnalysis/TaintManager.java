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

    }
    private boolean getResult(Object analysis) {
        List<SootMethod> entryPoints = getEntryPointMethods(); // Get all entry points
        Set<String> result = new HashSet<>();
        boolean containsMaliciousIndicator = false; // Flag to check for malicious indicators
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
            for (Unit unit : activeBody.getUnits()) {
                String unitString = unit.toString();
            // Check for sinks: ProcessBuilder.start, Runtime.exec (String), Runtime.exec (String[]), and Method.invoke
                if (unitString.contains("java.lang.ProcessBuilder") && unitString.contains("start")) {
                    containsMaliciousIndicator = true;
                } else if (unitString.contains("java.lang.Runtime") && unitString.contains("exec")) {
                    if (unitString.contains("(java.lang.String)") || unitString.contains("(java.lang.String[])")) {
                        containsMaliciousIndicator = true;
                    }
                }
//                else if (unitString.contains("java.lang.reflect.Method") && unitString.contains("invoke")) {
//                    containsMaliciousIndicator = true;
//                }
                else if (unitString.contains("ClassLoader")) {
                    containsMaliciousIndicator = true;
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

        return containsMaliciousIndicator;
    }


    public static byte[] readByteCode(String classFilePath) throws IOException {
        return Files.readAllBytes(Paths.get(classFilePath));
    }




    public boolean taint(String className, String userDefinedFolders) {
        AtomicBoolean check = new AtomicBoolean(false);
        Thread analysisThread = new Thread(() -> {
            try {
                JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(className, userDefinedFolders);
                check.set(getResult(analysis));
            } finally {
//                System.out.println("Taint analysis completed, thread shutting down.");

            }
        });
//

        analysisThread.start();

        try {
            analysisThread.join(5000);
        } catch (InterruptedException e) {
            System.err.println("Analysis thread was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        return check.get();

    }
}