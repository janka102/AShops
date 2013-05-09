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
package pl.austindev.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.keys.ASCommand;

public class AShopsCommandExecutor extends ASCommandExecutor {

	public AShopsCommandExecutor(AShops plugin) {
		super(plugin, ASCommand.ASHOPS);
	}

	@Override
	protected void run(CommandSender sender, Command command, String label,
			List<String> arguments) {
		StringBuilder list = new StringBuilder(ChatColor.GRAY.toString());
		int i = 0;
		for (ASCommand cmd : ASCommand.values())
			if (getPermissions().hasOneOf(sender, cmd.getPermissions())) {
				if ((i++ % 2) == 0)
					list.append(ChatColor.DARK_GRAY.toString());
				else
					list.append(ChatColor.GRAY.toString());
				list.append(getPlugin().$(cmd.getDescription()) + "\n");
			}
		list.delete(list.length() - 2, list.length());
		list.append(ChatColor.WHITE + "\n---");
		sender.sendMessage(list.toString());
	}
}
