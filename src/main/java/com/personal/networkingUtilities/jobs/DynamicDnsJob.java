package com.personal.networkingUtilities.jobs;

import com.personal.networkingUtilities.utils.Arguments;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.personal.networkingUtilities.utils.outputter.Outputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;

import static com.personal.networkingUtilities.jobs.JobRunner.BACKOFF_IN_SECONDS;

public class DynamicDnsJob implements BaseJob {

    private static final int DEFAULT_TTL_IN_SECONDS = 300;
    private static final String HOSTED_ZONE_ID = "ZEJDJNHNN5KF6";

    private final Route53Client route53Client;

    private final Outputter outputter;

    private static final Logger logger = LoggerFactory.getLogger(DynamicDnsJob.class);

    public DynamicDnsJob(final Route53Client route53Client, final Outputter outputter) {
        this.route53Client = route53Client;
        this.outputter = outputter;
    }

    @Override
    public boolean runJob(final Arguments arguments) {
        final Optional<String> maybeRecordName = arguments.getArgumentValue(Arguments.RECORD_NAME_ARG);
        final Optional<String> maybeRecordType = arguments.getArgumentValue(Arguments.RECORD_TYPE_ARG);
        if (maybeRecordName.isEmpty() || maybeRecordType.isEmpty()) {
            logger.error("Missing arguments. Either record name or record type is missing");
            return false;
        }

        final Optional<String> maybeRecordTtl = arguments.getArgumentValue(Arguments.RECORD_TTL_ARG);
        int ttlInSeconds;
        if (maybeRecordTtl.isPresent()) {
            try {
                ttlInSeconds = Integer.parseInt(maybeRecordTtl.get());
            } catch (NumberFormatException ex) {
                logger.error("Unable to parse integer from {}", maybeRecordTtl.get());
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

        if (!this.doesDnsEntryMatch(maybeRecordName.get(), maybeRecordType.get(), currentWanIp)) {
            return this.updateAliasRecord(maybeRecordName.get(), maybeRecordType.get(), currentWanIp, ttlInSeconds);
        } else {
            logger.info("DNS entry is up to date: {}. Nothing to do", currentWanIp);
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
            logger.error("Error finding my IP address");
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
        logger.info("Updated {} of type {} to the new value {} with ttl {}", recordName, recordType, newIpAddress, ttlInSeconds);
        final String changeInfoId = response.changeInfo().id();

        try {
            while (!isChangeInfoInSync(changeInfoId)) {
                logger.info("Change info, {}, not updated yet waiting {} seconds", changeInfoId, BACKOFF_IN_SECONDS);
                Thread.sleep(BACKOFF_IN_SECONDS);
            }
            return true;
        } catch (InterruptedException ex) {
            logger.error("Interrupted while waiting for DNS update to sync.", ex);
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
