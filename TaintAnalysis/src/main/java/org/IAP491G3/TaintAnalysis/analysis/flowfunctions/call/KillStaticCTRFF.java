package org.IAP491G3.TaintAnalysis.analysis.flowfunctions.call;

import org.IAP491G3.TaintAnalysis.analysis.data.DFF;
import heros.FlowFunction;
import soot.jimple.StaticFieldRef;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KillStaticCTRFF implements FlowFunction<DFF> {

    @Override
    public Set<DFF> computeTargets(DFF source) {
        if (source.getValue() instanceof StaticFieldRef) {
            return Collections.emptySet();
        }
        Set<DFF> res = new HashSet<>();
        res.add(source);
        return res;
    }
}
