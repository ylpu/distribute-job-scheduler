package com.ylpu.kepler.scheduler.enums;

/**
 *
 *                                     |------------kill---------------------|
 *                                     |                                     |
 * submit -> pending --> waiting -> running --> success -------------------->end
 *                          ^                        |                       ^
 *                          |                       fail                     |
 *                          |_________retry__________|__exceed retry times___|
 */
public enum TaskState {
    SUBMIT(1), PENDING(2), WAITING(3), RUNNING(4), KILL(5), SUCCESS(6), FAIL(7);
    
    private int code;
    
    private TaskState(int code) {
        this.code = code;
    }
    
    public static TaskState getTaskType(int code) {
        for(TaskState taskState : TaskState.values()) {
            if(taskState.code == code) {
                return taskState;
            }
        }
        throw new IllegalArgumentException("unsupported task state " + code);
    }
    
    public int getCode() {
        return this.code;
    }
}
