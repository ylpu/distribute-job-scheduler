package com.ylpu.thales.scheduler.enums;

public enum JobPriority {

    LOW(1), MEDIUM(2), HIGH(3);

    private int priority;

    private JobPriority(int priority) {
        this.priority = priority;
    }

    public static JobPriority getJobPriority(int priority) {
        for (JobPriority jobPriority : JobPriority.values()) {
            if (jobPriority.priority == priority) {
                return jobPriority;
            }
        }
        throw new IllegalArgumentException("unsupported job priority " + priority);
    }

    public static JobPriority getJobPriority(String name) {
        for (JobPriority jobPriority : JobPriority.values()) {
            if (jobPriority.toString().equalsIgnoreCase(name)) {
                return jobPriority;
            }
        }
        throw new IllegalArgumentException("unsupported node status " + name);
    }

    public int getPriority() {
        return this.priority;
    }
}
