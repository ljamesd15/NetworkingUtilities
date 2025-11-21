package org.networkingUtilities.model;

import lombok.Builder;

@Builder
public record WebhookSecret(String webhookUrl) {
}
