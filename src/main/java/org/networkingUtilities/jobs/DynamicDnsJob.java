package org.networkingUtilities.jobs;

import software.amazon.awssdk.services.route53.Route53Client;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class DynamicDnsJob implements BaseJob {

    private static final int DEFAULT_TTL_IN_SECONDS = 300;

    private final Route53Client route53Client;

    @Inject
    public DynamicDnsJob(@Named("DynamicDns") final Route53Client route53Client) {
        this.route53Client = route53Client;
    }

    @Override
    public boolean runJob(final List<String> arguments) {
        if (arguments.size() < 2) {
            System.out.println("Insufficient arguments. You must provide at least the record name and type");
            return false;
        }
        final String recordName = arguments.get(0);
        final String recordType = arguments.get(1);
        int ttlInSeconds;
        if (arguments.size() > 2) {
            try {
                ttlInSeconds = Integer.parseInt(arguments.get(2));
            } catch (NumberFormatException ex) {
                System.out.printf("Unable to parse integer from %s%n", arguments.get(2));
                return false;
            }
        } else {
            ttlInSeconds = DEFAULT_TTL_IN_SECONDS;
        }
        System.out.println(recordName);
        System.out.println(recordType);
        System.out.println(ttlInSeconds);
        return false;
    }
}
