package com.personal.networkingUtilities.config;

import dagger.Module;
import dagger.Provides;
import com.personal.networkingUtilities.jobs.DynamicDnsJob;
import com.personal.networkingUtilities.jobs.ServerHealthJob;
import com.personal.networkingUtilities.utils.outputter.Outputter;
import software.amazon.awssdk.services.route53.Route53Client;

import javax.inject.Named;

@Module
public class JobModule {

    @Provides
    public ServerHealthJob getServerHealthJob(final Outputter outputter) {
        return new ServerHealthJob(outputter);
    }

    @Provides
    public DynamicDnsJob getDynamicDnsJob(@Named("DynamicDns") final Route53Client route53Client, final Outputter outputter) {
        return new DynamicDnsJob(route53Client, outputter);
    }
}
