package org.networkingUtilities.jobs;

import java.util.List;

public interface BaseJob {

    boolean runJob(final List<String> arguments);
}
