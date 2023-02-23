package org.networkingUtilities;

import org.networkingUtilities.utils.JobRunner;

import java.util.Arrays;
import java.util.Optional;

public class Main {

    public static void main(final String[] args) {
        if (args.length <= 0) {
            System.out.println("Invalid number of arguments, there must be at least one.");
            return;
        }

        final Optional<JobRunner.JobRunType> maybeJobRunType = getJobRunType(args[0]);

        if (maybeJobRunType.isEmpty()) {
            System.out.printf("Invalid job run type: %s\n", args[0]);
            return;
        }

        final JobRunner jobRunner = new JobRunner(maybeJobRunType.get(), Arrays.copyOfRange(args, 1, args.length));
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
