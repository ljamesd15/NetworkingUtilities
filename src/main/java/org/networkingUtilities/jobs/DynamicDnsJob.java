package org.networkingUtilities.jobs;

import java.util.List;

public class DynamicDnsJob implements BaseJob {

    private static final int DEFAULT_TTL_IN_SECONDS = 300;

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
