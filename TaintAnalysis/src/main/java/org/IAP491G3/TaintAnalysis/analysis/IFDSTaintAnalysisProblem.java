package org.IAP491G3.TaintAnalysis.analysis;

import org.IAP491G3.TaintAnalysis.analysis.data.DFF;
import org.IAP491G3.TaintAnalysis.analysis.flowfunctions.CallFlowFunctionProvider;
import org.IAP491G3.TaintAnalysis.analysis.flowfunctions.NormalFlowFunctionProvider;
import org.IAP491G3.TaintAnalysis.analysis.flowfunctions.ReturnFlowFunctionProvider;
import org.IAP491G3.TaintAnalysis.analysis.flowfunctions.CallToReturnFlowFunctionProvider;
import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import soot.*;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import org.IAP491G3.TaintAnalysis.Utils.CFGUtil;

import java.util.*;

public class IFDSTaintAnalysisProblem extends DefaultJimpleIFDSTabulationProblem<DFF, InterproceduralCFG<Unit, SootMethod>> {

    private List<SootMethodRef> sources;
    private List<SootMethodRef> sinks;

    protected InterproceduralCFG<Unit, SootMethod> icfg;


    public IFDSTaintAnalysisProblem(InterproceduralCFG<Unit, SootMethod> icfg, List<SootMethodRef> sources, List<SootMethodRef> sinks) {
        super(icfg);
        this.icfg = icfg;
        this.sources = sources;
        this.sinks = sinks;
    }

    @Override
    protected FlowFunctions<Unit, DFF, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Unit, DFF, SootMethod>() {
            @Override
            public FlowFunction<DFF> getNormalFlowFunction(Unit curr, Unit succ) {
                NormalFlowFunctionProvider ffp = new NormalFlowFunctionProvider(icfg.getMethodOf(curr), curr, zeroValue());
                return ffp.getFlowFunction();
            }

            @Override
            public FlowFunction<DFF> getCallFlowFunction(Unit callStmt, SootMethod dest) {
                CallFlowFunctionProvider ffp = new CallFlowFunctionProvider(callStmt, dest, zeroValue());
                return ffp.getFlowFunction();
            }

            @Override
            public FlowFunction<DFF> getReturnFlowFunction(Unit callSite, SootMethod calleeMethod, Unit exitStmt, Unit returnSite) {
                ReturnFlowFunctionProvider ffp = new ReturnFlowFunctionProvider(callSite, exitStmt, icfg.getMethodOf(callSite), icfg.getMethodOf(exitStmt));
                return ffp.getFlowFunction();
            }

            @Override
            public FlowFunction<DFF> getCallToReturnFlowFunction(Unit callSite, Unit returnSite) {
                CallToReturnFlowFunctionProvider ffp = new CallToReturnFlowFunctionProvider(icfg.getMethodOf(callSite), callSite, zeroValue(), sources);
                return ffp.getFlowFunction();
            }
        };
    }

    @Override
    protected DFF createZeroValue() {
        return DFF.asDFF(new JimpleLocal("<<zero>>", NullType.v()));
    }

    @Override
    public Map<Unit, Set<DFF>> initialSeeds() {
        // Create a map to hold the initial seeds
        Map<Unit, Set<DFF>> initialSeedsMap = new HashMap<>();

        // Iterate through all application classes in the Scene
        for (SootClass c : Scene.v().getApplicationClasses()) {
            // Iterate through all methods in the class
            for (SootMethod m : c.getMethods()) {
                if (!m.hasActiveBody()) {
                    continue;
                }

                // For each method with an ActiveBody,   create a unit graph
                DirectedGraph<Unit> unitGraph = new BriefUnitGraph(m.getActiveBody());

                // Add the head of the graph (entry point) as a seed
                initialSeedsMap.putAll(DefaultSeeds.make(Collections.singleton(CFGUtil.getHead(unitGraph)), zeroValue()));
            }
        }

        return initialSeedsMap.isEmpty() ? Collections.emptyMap() : initialSeedsMap;
    }



}
