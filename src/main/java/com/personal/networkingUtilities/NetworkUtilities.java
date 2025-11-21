package com.personal.networkingUtilities;

import com.personal.networkingUtilities.jobs.JobRunner;
import com.personal.networkingUtilities.jobs.JobType;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class NetworkUtilities {


    private static final Logger logger = LoggerFactory.getLogger(NetworkUtilities.class);

    public static void main(final String[] args) {
        final NetworkUtilities networkUtilities = new NetworkUtilities();
        networkUtilities.start(Arrays.stream(args).toList());
    }

    private void start(final List<String> arguments) {
        if (arguments.size() <= 0) {
            logger.error("Invalid number of arguments, there must be at least one.");
            return;
        }

        final Optional<JobType> maybeJobRunType = getJobType(arguments.get(0));

        if (maybeJobRunType.isEmpty()) {
            logger.error("Invalid job run type: {}", arguments.get(0));
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
