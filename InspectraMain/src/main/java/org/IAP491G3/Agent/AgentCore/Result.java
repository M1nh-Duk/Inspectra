package org.IAP491G3.Agent.AgentCore;

import java.util.List;
import java.util.Map;

import static org.IAP491G3.Agent.Utils.LogUtils.getEventTime;

public class Result {
    private final String eventTimeStamp;
    private final String type;
    private int riskScore;
    private String fullPathClassName;
    private Boolean taintResult;
    private Boolean isRetransformed;
    private Boolean isJSP;
    private Boolean isJspFileDeleted;
    private Map<String, List<String>>  filterResult;


    public void setJSP(Boolean JSP) {
        isJSP = JSP;
    }

    public Result(String fullPathClassName,  String type) {
        this.eventTimeStamp = String.valueOf(getEventTime());
        this.riskScore = 0;
        this.fullPathClassName = fullPathClassName;
        this.type = type;
        this.isJSP = false;
        this.taintResult = false;
        this.isRetransformed = false;
        this.isJspFileDeleted = false;
    }
    public Map<String, List<String>> getFilterResult() {return filterResult;    }

    public void setFilterResult(Map<String, List<String>> filterResult) {
        this.filterResult = filterResult;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getFullPathClassName() {
        return fullPathClassName;
    }

    public void setTaintResult(Boolean taintResult) {
        this.taintResult = taintResult;
    }

    public void setRetransformedStatus(Boolean isRetransformed) {
        this.isRetransformed = isRetransformed;
    }

    public void setJspDeleteStatus(Boolean isJspFileDeleted) {this.isJspFileDeleted = isJspFileDeleted;}



}
