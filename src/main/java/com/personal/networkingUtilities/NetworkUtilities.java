package org.networkingUtilities;

import lombok.NoArgsConstructor;
import org.networkingUtilities.jobs.JobRunner;
import org.networkingUtilities.jobs.JobType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class NetworkUtilities {

    public static void main(final String[] args) {
        final NetworkUtilities networkUtilities = new NetworkUtilities();
        networkUtilities.start(Arrays.stream(args).collect(Collectors.toUnmodifiableList()));
    }

    private void start(final List<String> arguments) {
        if (arguments.size() <= 0) {
            System.out.println("Invalid number of arguments, there must be at least one.");
            return;
        }

        final Optional<JobType> maybeJobRunType = getJobType(arguments.get(0));

        if (maybeJobRunType.isEmpty()) {
            System.out.printf("Invalid job run type: %s%n", arguments.get(0));
            return;
        }

        new JobRunner().runJob(maybeJobRunType.get(), arguments.subList(1, arguments.size()));
    }

    private Optional<JobType> getJobType(final String jobArgument) {
        try {
            return Optional.of(JobType.fromString(jobArgument));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }


}
