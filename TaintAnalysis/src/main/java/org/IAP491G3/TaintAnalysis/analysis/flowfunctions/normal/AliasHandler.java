package org.IAP491G3.TaintAnalysis.analysis.flowfunctions.normal;

import org.IAP491G3.TaintAnalysis.analysis.data.DFF;

import java.util.Set;

public interface AliasHandler {

    default void handleAliases(Set<DFF> res){

    }
}
