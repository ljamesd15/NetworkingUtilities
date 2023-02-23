package org.networkingUtilities;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.networkingUtilities.utils.JobRunner;

public class Main {

    public static void main(final String[] args) {
        if (args.length <= 0) {
            System.out.println("Invalid number of arguments, there must be at least one.");
            return;
        }

        final Optional<JobRunner.JobRunType> maybeJobRunType = getJobRunType(args[0]);

        if (maybeJobRunType.isEmpty()) {
            System.out.printf("Invalid job run type: %s%n", args[0]);
            return;
        }

        final JobRunner jobRunner = JobRunner.builder()
                .jobRunType(maybeJobRunType.get())
                .arguments(Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).collect(Collectors.toUnmodifiableList()))
                .build();
        jobRunner.runJob();
    }

    private static Optional<JobRunner.JobRunType> getJobRunType(final String jobRunArgument) {
        try {
            return Optional.of(JobRunner.JobRunType.fromString(jobRunArgument));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
