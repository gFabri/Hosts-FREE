package com.github.bfabri.hosts.commands.utils.framework;

import com.github.bfabri.hosts.commands.utils.ArgumentExecutor;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

public abstract class BaseCommand extends ArgumentExecutor {
    private static final Pattern USAGE_REPLACER_PATTERN;

    static {
        USAGE_REPLACER_PATTERN = Pattern.compile("(command)", 16);
    }

    private final String name;
    private final String description;
    private String[] aliases;
    private String usage;

    public boolean onlyPlayer;
    public boolean onlyConsole;

    public BaseCommand(String name, String description) {
        super(name);
        this.name = name;
        this.description = description;
        this.onlyPlayer = false;
        this.onlyConsole = false;
    }

    public String getPermission() {
        return "hosts.command." + this.name;
    }

    public boolean isOnlyPlayer() {
        return onlyPlayer;
    }

    public boolean isOnlyConsole() {
        return onlyConsole;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUsage() {
        if (this.usage == null) {
            this.usage = "";
        }
        return BaseCommand.USAGE_REPLACER_PATTERN.matcher(this.usage).replaceAll(this.name);
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getUsage(String label) {
        return BaseCommand.USAGE_REPLACER_PATTERN.matcher(this.usage).replaceAll(label);
    }

    public String[] getAliases() {
        if (this.aliases == null) {
            this.aliases = ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return Arrays.copyOf(this.aliases, this.aliases.length);
    }

    protected void setAliases(String[] aliases) {
        this.aliases = aliases;
    }
}
