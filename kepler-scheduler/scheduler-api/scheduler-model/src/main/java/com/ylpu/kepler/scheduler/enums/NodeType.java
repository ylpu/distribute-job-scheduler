package com.ylpu.kepler.scheduler.enums;

public enum NodeType {
    
    MASTER(1), WORKER(2);
    
    private int code;
    
    private NodeType(int code) {
        this.code = code;
    }
    
    public static NodeType getNodeType(int code) {
        for(NodeType nodeType : NodeType.values()) {
            if(nodeType.code == code) {
                return nodeType;
            }
        }
        throw new IllegalArgumentException("unsupported node type " + code);
    }
    
    public int getCode() {
        return this.code;
    }
}
