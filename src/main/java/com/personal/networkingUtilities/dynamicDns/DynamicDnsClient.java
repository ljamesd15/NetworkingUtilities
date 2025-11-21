package org.networkingUtilities.dynamicDns;

import lombok.Builder;

@Builder
public class DynamicDnsClient {

    private final String recordName;


    public boolean checkAndUpdateDnsRecord(final String ipAddress) {
        return false;
    }
}
