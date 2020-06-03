package com.ylpu.thales.scheduler.enums;

/**
 *
 * |------------kill---------------------| | | submit -> scheduled --> waiting ->
 * running --> success -------------------->end ^ | ^ | fail |
 * |_________retry__________|__exceed retry times___|
 */
public enum TaskState {
    SUBMIT(1), SCHEDULED(2), WAITING_DEPENDENCY(3), QUEUED(4), WAITING_RESOURCE(5), RUNNING(6), KILL(7), SUCCESS(8), FAIL(9);

    private int code;

    private TaskState(int code) {
        this.code = code;
    }

    public static TaskState getTaskStateById(int code) {
        for (TaskState taskState : TaskState.values()) {
            if (taskState.code == code) {
                return taskState;
            }
        }
        throw new IllegalArgumentException("unsupported task state " + code);
    }

    public int getCode() {
        return this.code;
    }
}
