/*
 * AShops Bukkit Plugin
 * Copyright 2013 Austin Reuter (_austinho)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.austindev.mc;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class ACommandExecutor<T extends APlugin> implements
		CommandExecutor {
	private final T plugin;
	private final ACommand pluginCommand;
	private final AMessage permissionsMessage;
	private final AMessage notPlayerMessage;

	public ACommandExecutor(T plugin, ACommand aCommand,
			AMessage permissionMessage, AMessage notPlayerMessage) {
		this.plugin = plugin;
		this.pluginCommand = aCommand;
		this.permissionsMessage = permissionMessage;
		this.notPlayerMessage = notPlayerMessage;
	}

	public static <U extends Enum<U> & ACommand> void register(APlugin plugin,
			Class<U> commands) {
		for (U command : commands.getEnumConstants())
			register(plugin, command);
	}

	@Override
	public final boolean onCommand(CommandSender sender, Command command,
			String label, String[] arguments) {
		if (getPermissions().hasOneOf(sender, getACommand().getPermissions())) {
			if (!PlayerUtil.isPlayer(sender) && getACommand().isForPlayer()) {
				tell(sender, notPlayerMessage);
				return true;
			}
			if (arguments.length >= pluginCommand.getMinArgumentsNumber())
				if (arguments.length <= pluginCommand.getMaxArgumentsNumber())
					run(sender, command, label, Arrays.asList(arguments));
				else
					tell(sender, ChatColor.RED, pluginCommand.getDescription());
			else
				tell(sender, ChatColor.RED, pluginCommand.getDescription());
		} else {
			tell(sender, getPermissionsMessage());
		}
		return true;
	}

	protected abstract void run(CommandSender sender, Command command,
			String label, List<String> arguments);

	protected T getPlugin() {
		return plugin;
	}

	protected ACommand getACommand() {
		return pluginCommand;
	}

	protected AMessage getPermissionsMessage() {
		return permissionsMessage;
	}

	protected AMessage getNotPlayerMessage() {
		return notPlayerMessage;
	}

	protected void tell(CommandSender sender, AMessage key, Object... arguments) {
		sender.sendMessage(getPlugin().$(key, arguments));
	}

	protected void tell(CommandSender sender, ChatColor color, AMessage key,
			Object... arguments) {
		sender.sendMessage(color + getPlugin().$(key, arguments));
	}

	protected PermissionsProvider getPermissions() {
		return plugin.getPermissions();
	}

	protected EconomyProvider getEconomy() {
		return plugin.getEconomy();
	}

	private static <T extends Enum<T> & ACommand> void register(APlugin plugin,
			T command) {
		try {
			plugin.getCommand(command.name()).setExecutor(
					command.getExecutor().getConstructor(plugin.getClass())
							.newInstance(plugin));
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
