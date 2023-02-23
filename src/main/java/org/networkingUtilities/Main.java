package org.networkingUtilities;

import org.networkingUtilities.jobs.JobRunner;
import org.networkingUtilities.jobs.JobType;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    public static void main(final String[] args) {
        if (args.length <= 0) {
            System.out.println("Invalid number of arguments, there must be at least one.");
            return;
        }

        final Optional<JobType> maybeJobRunType = getJobType(args[0]);

        if (maybeJobRunType.isEmpty()) {
            System.out.printf("Invalid job run type: %s%n", args[0]);
            return;
        }

        final JobRunner jobRunner = JobRunner.builder()
                .jobType(maybeJobRunType.get())
                .arguments(Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).collect(Collectors.toUnmodifiableList()))
                .build();
        jobRunner.runJob();
    }

    private static Optional<JobType> getJobType(final String jobArgument) {
        try {
            return Optional.of(JobType.fromString(jobArgument));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
