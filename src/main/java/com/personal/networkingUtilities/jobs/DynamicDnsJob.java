package org.networkingUtilities.jobs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.networkingUtilities.utils.outputter.Outputter;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.ChangeStatus;
import software.amazon.awssdk.services.route53.model.GetChangeRequest;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import software.amazon.awssdk.services.route53.model.TestDnsAnswerRequest;
import software.amazon.awssdk.services.route53.model.TestDnsAnswerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import static org.networkingUtilities.jobs.JobRunner.BACKOFF_IN_SECONDS;

public class DynamicDnsJob implements BaseJob {

    private static final int DEFAULT_TTL_IN_SECONDS = 300;
    private static final String HOSTED_ZONE_ID = "ZEJDJNHNN5KF6";

    @SuppressFBWarnings("EI_EXPOSE_REP")
    private final Route53Client route53Client;

    private final Outputter outputter;

    @Inject
    public DynamicDnsJob(@Named("DynamicDns") final Route53Client route53Client, final Outputter outputter) {
        this.route53Client = route53Client;
        this.outputter = outputter;
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

        String currentWanIp;
        try {
            currentWanIp = this.findMyWanIp();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        if (!this.doesDnsEntryMatch(recordName, recordType, currentWanIp)) {
            return this.updateAliasRecord(recordName, recordType, currentWanIp, ttlInSeconds);
        } else {
            System.out.printf("DNS entry is up to date: %s. Nothing to do%n", currentWanIp);
        }
        return true;
    }

    private boolean doesDnsEntryMatch(final String recordName, final String recordType, final String expectedIpAddress) {
        final TestDnsAnswerResponse response = this.route53Client.testDNSAnswer(TestDnsAnswerRequest.builder()
                        .recordName(recordName)
                        .recordType(recordType)
                        .hostedZoneId(HOSTED_ZONE_ID)
                .build());
        return response.recordData().stream().anyMatch(expectedIpAddress::equals);
    }

    /**
     * Hack to find the external IP address of this machine.
     *
     * @return The IP address of this machine according to AWS
     * @throws IOException If we were unable to determine the external IP address of this machine
     */
    private String findMyWanIp() throws IOException {
        final HttpGet httpGet = new HttpGet("http://checkip.amazonaws.com/");
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            final CloseableHttpResponse response = client.execute(httpGet);
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                return br.readLine();
            }
        } catch (IOException ex) {
            System.out.println("Error finding my IP address");
            throw ex;
        }
    }

    private boolean updateAliasRecord(final String recordName, final String recordType, final String newIpAddress, final long ttlInSeconds) {
        final ChangeResourceRecordSetsResponse response = this.route53Client.changeResourceRecordSets(ChangeResourceRecordSetsRequest.builder()
                .hostedZoneId(HOSTED_ZONE_ID)
                .changeBatch(ChangeBatch.builder()
                        .changes(Change.builder()
                                .action(ChangeAction.UPSERT)
                                .resourceRecordSet(ResourceRecordSet.builder()
                                        .name(recordName)
                                        .type(recordType)
                                        .ttl(ttlInSeconds)
                                        .resourceRecords(ResourceRecord.builder()
                                                .value(newIpAddress)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build());
        System.out.printf("Updated %s of type %s to the new value %s with ttl %d%n", recordName, recordType, newIpAddress, ttlInSeconds);
        final String changeInfoId = response.changeInfo().id();

        try {
            while (!isChangeInfoInSync(changeInfoId)) {
                System.out.printf("Change info, %s, not updated yet waiting %d seconds%n", changeInfoId, BACKOFF_IN_SECONDS);
                Thread.sleep(BACKOFF_IN_SECONDS);
            }
            return true;
        } catch (InterruptedException ex) {
            System.out.println("Interrupted while waiting for DNS update to sync.");
            ex.printStackTrace();
            return false;
        }
    }

    private boolean isChangeInfoInSync(final String changeInfoId) {
        return ChangeStatus.INSYNC.equals(
                this.route53Client.getChange(GetChangeRequest.builder()
                                .id(changeInfoId)
                                .build())
                        .changeInfo()
                        .status()
        );
    }
}
