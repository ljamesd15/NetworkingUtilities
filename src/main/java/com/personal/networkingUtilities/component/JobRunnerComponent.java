package com.personal.networkingUtilities.component;

import com.personal.networkingUtilities.config.AwsConfig;
import com.personal.networkingUtilities.config.SerializationConfig;
import com.personal.networkingUtilities.config.SlackConfig;
import dagger.Component;
import com.personal.networkingUtilities.config.OutputterConfig;
import com.personal.networkingUtilities.config.JobConfig;
import com.personal.networkingUtilities.jobs.JobRunner;

import javax.inject.Singleton;

@Singleton
@Component(modules = { JobConfig.class, AwsConfig.class, SerializationConfig.class, OutputterConfig.class, SlackConfig.class})
public interface JobRunnerComponent {

    JobRunner inject(final JobRunner jobRunner);
}
