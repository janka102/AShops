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

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.data.DataAccessException;
import pl.austindev.ashops.keys.ASCommand;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.mc.PlayerUtil;

public class AClearCommandExecutor extends ASCommandExecutor {

	public AClearCommandExecutor(AShops plugin) {
		super(plugin, ASCommand.ACLEAR);
	}

	@Override
	protected void run(CommandSender sender, Command command, String label,
			List<String> arguments) {
		try {
			String arg = arguments.get(0);
			if (arg.equals("*")) {
				getShopsManager().clearPlayerShops();
				getShopsManager().clearServerShops();
				tell(sender, ASMessage.SHOPS_CLEARED);
			} else if (arg.equalsIgnoreCase(":ss:")) {
				getShopsManager().clearServerShops();
				tell(sender, ASMessage.SHOPS_CLEARED);
			} else if (arg.equalsIgnoreCase(":ps:")) {
				getShopsManager().clearPlayerShops();
				tell(sender, ASMessage.SHOPS_CLEARED);
			} else if (PlayerUtil.isValidPlayerName(arg)) {
				if (getShopsManager().clearPlayerShops(arg))
					tell(sender, ASMessage.SHOPS_CLEARED);
				else
					tell(sender, ASMessage.NOT_OWNER_NAME);
			} else {
				tell(sender, ASMessage.INVALID_PLAYER);
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
			tell(sender, ASMessage.ERROR);
		}
	}
}
