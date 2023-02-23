package org.networkingUtilities.config;

import dagger.Component;
import org.networkingUtilities.config.modules.AwsModule;
import org.networkingUtilities.config.modules.JobModule;
import org.networkingUtilities.jobs.JobRunner;

import javax.inject.Singleton;

@Singleton
@Component(modules = { JobModule.class, AwsModule.class })
public interface JobRunnerComponent {

    JobRunner inject(final JobRunner jobRunner);
}
