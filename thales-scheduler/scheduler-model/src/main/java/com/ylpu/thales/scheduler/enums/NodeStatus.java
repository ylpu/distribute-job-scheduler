package com.ylpu.thales.scheduler.enums;

public enum NodeStatus {
    
    ADDED(1), UPDATED(2), REMOVED(3);
    
    private int code;
    
    private NodeStatus(int code) {
        this.code = code;
    }
    
    public static NodeStatus getNodeStatus(int code) {
        for(NodeStatus nodeStatus : NodeStatus.values()) {
            if(nodeStatus.code == code) {
                return nodeStatus;
            }
        }
        throw new IllegalArgumentException("unsupported node status " + code);
    }
    
    public int getCode() {
        return this.code;
    }
}
