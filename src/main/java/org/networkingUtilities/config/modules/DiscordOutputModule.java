package org.networkingUtilities.config.modules;

import dagger.Binds;
import dagger.Module;
import org.networkingUtilities.utils.outputter.DiscordWebhook;
import org.networkingUtilities.utils.outputter.Outputter;

@Module
public abstract class DiscordOutputModule {

    @Binds
    abstract Outputter getOutputter(DiscordWebhook outputter);
}
