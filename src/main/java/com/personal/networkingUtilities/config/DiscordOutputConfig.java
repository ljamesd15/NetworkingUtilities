package com.personal.networkingUtilities.config;

import com.personal.networkingUtilities.utils.outputter.DiscordWebhook;
import com.personal.networkingUtilities.utils.outputter.Outputter;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class DiscordOutputModule {

    @Binds
    abstract Outputter getOutputter(DiscordWebhook outputter);
}
