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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.keys.ASCommand;
import pl.austindev.ashops.keys.ASMessage;

public class ARepairCommandExecutor extends ASCommandExecutor {

	public ARepairCommandExecutor(AShops plugin) {
		super(plugin, ASCommand.AREPAIR);
	}

	@Override
	protected void run(CommandSender sender, Command command, String label,
			List<String> arguments) {
		Player player = (Player) sender;
		tell(player,
				getShopsManager().toggleRepairMode(player.getName()) ? ASMessage.REPAIR_MODE
						: ASMessage.NORMAL_MODE);

	}
}