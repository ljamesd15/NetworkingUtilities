package com.personal.networkingUtilities.utils;

import lombok.Getter;

import java.util.List;
import java.util.Optional;

public class Arguments {

    @Getter
    final List<String> argStrings;

    public Arguments(final List<String> argStrings) {
        this.argStrings = argStrings;
    }

    // General
    public static final String ARG_FORMAT = "--%s=";
    public static final String JOB_TYPE_ARG = "job";

    // Server health
    public static final String HOSTNAME_ARG = "hostname";

    public static final String PORT_ARG = "port";

    public static final String SERVER_RESTART_FILE_PATH_ARG = "serverRestartFilepath";

    public static final String SERVER_TYPE_ARG = "serverType";

    // DDNS
    public static final String RECORD_NAME_ARG = "recordName";

    public static final String RECORD_TYPE_ARG = "recordType";

    public static final String RECORD_TTL_ARG = "ttl";

    public Optional<String> getArgumentValue(final String argumentName) {
        String argPrefix = String.format(ARG_FORMAT, argumentName);

        return argStrings.stream()
                .filter((String argString) -> argString.startsWith(argPrefix))
                .findFirst()
                .map((String argString) -> argString.substring(argPrefix.length()));
    }

    public String get(int index) {
        return this.argStrings.get(0);
    }

    public int size() {
        return this.argStrings.size();
    }
}
