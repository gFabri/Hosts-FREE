package com.github.bfabri.hosts.commands.utils.framework;

public interface CommandManager {
	boolean containsCommand(BaseCommand p0);

	void registerAll(BaseCommandModule p0);

	void registerCommand(BaseCommand p0);

	void registerCommands(BaseCommand[] p0);

	void unregisterCommand(BaseCommand p0);

	BaseCommand getCommand(String p0);
}
