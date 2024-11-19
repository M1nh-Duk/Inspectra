package org.IAP491G3.TaintAnalysis.analysis.flowfunctions;

import heros.FlowFunction;

public interface FlowFunctionProvider<D> {
    FlowFunction<D> getFlowFunction();
}
