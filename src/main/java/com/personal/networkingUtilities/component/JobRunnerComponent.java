package com.personal.networkingUtilities.config;

import dagger.Component;
import com.personal.networkingUtilities.config.modules.AwsModule;
import com.personal.networkingUtilities.config.modules.DiscordOutputModule;
import com.personal.networkingUtilities.config.modules.JobModule;
import com.personal.networkingUtilities.jobs.JobRunner;

import javax.inject.Singleton;

@Singleton
@Component(modules = { JobModule.class, AwsModule.class, DiscordOutputModule.class })
public interface JobRunnerComponent {

    JobRunner inject(final JobRunner jobRunner);
}
