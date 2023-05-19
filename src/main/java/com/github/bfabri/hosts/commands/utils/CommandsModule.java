package com.github.bfabri.hosts.commands.utils;

import com.github.bfabri.hosts.commands.host.HostExecutor;
import com.github.bfabri.hosts.commands.utils.framework.BaseCommandModule;

public class CommandsModule extends BaseCommandModule {
	public CommandsModule() {
		this.commands.add(new HostExecutor());
	}
}
