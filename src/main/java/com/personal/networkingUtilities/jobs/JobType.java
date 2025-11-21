package org.networkingUtilities.jobs;

public enum JobType {
    SERVER_HEALTH("ServerHealth"),
    DYNAMIC_DNS("DynamicDns");

    public final String label;

    JobType(final String label) {
        this.label = label;
    }

    public static JobType fromString(String label) {
        for (JobType jobType : JobType.values()) {
            if (jobType.label.equalsIgnoreCase(label)) {
                return jobType;
            }
        }
        throw new IllegalArgumentException(String.format("No JobRunType with label %s found.", label));
    }
}
