package com.personal.networkingUtilities;

import com.personal.networkingUtilities.jobs.JobRunner;
import com.personal.networkingUtilities.jobs.JobType;
import com.personal.networkingUtilities.utils.Arguments;
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
        final Arguments arguments = new Arguments(Arrays.stream(args).toList());
        networkUtilities.start(arguments);
    }

    private void start(final Arguments arguments) {
        Optional<String> maybeJobType = arguments.getArgumentValue(Arguments.JOB_TYPE_ARG);
        if (maybeJobType.isEmpty()) {
            logger.error("Must specify the job type");
            return;
        }

        final Optional<JobType> maybeJobRunType = getJobType(maybeJobType.get());
        if (maybeJobRunType.isEmpty()) {
            logger.error("Invalid job run type: {}", maybeJobType.get());
            return;
        }

        new JobRunner().runJob(maybeJobRunType.get(), arguments);
    }

    private Optional<JobType> getJobType(final String jobArgument) {
        try {
            return Optional.of(JobType.fromString(jobArgument));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }


}
