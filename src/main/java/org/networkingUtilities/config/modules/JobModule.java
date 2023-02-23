package org.networkingUtilities.config.modules;

import dagger.Module;
import dagger.Provides;
import org.networkingUtilities.jobs.DynamicDnsJob;
import org.networkingUtilities.jobs.ServerHealthJob;
import software.amazon.awssdk.services.route53.Route53Client;

import javax.inject.Named;

@Module
public class JobModule {

    @Provides
    public ServerHealthJob getServerHealthJob() {
        return new ServerHealthJob();
    }

    @Provides
    public DynamicDnsJob getDynamicDnsJob(@Named("DynamicDns") final Route53Client route53Client) {
        return new DynamicDnsJob(route53Client);
    }
}
