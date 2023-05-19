package com.github.bfabri.hosts.commands.utils.framework;

import com.google.common.collect.Sets;

import java.util.Set;

public abstract class BaseCommandModule {
    protected Set<BaseCommand> commands;
    protected boolean enabled;

    public BaseCommandModule() {
        this.commands = Sets.newHashSet();
        this.enabled = true;
    }

    Set<BaseCommand> getCommands() {
        return this.commands;
    }

    void unregisterCommand(BaseCommand command) {
        this.commands.remove(command);
    }

    void unregisterCommands() {
        this.commands.clear();
    }

    boolean isEnabled() {
        return this.enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
