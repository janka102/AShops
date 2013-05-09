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
import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.mc.PlayerUtil;

public class AShopCommandExecutor extends ASCommandExecutor {
	public AShopCommandExecutor(AShops plugin) {
		super(plugin, ASCommand.ASHOP);
	}

	@Override
	protected void run(CommandSender sender, Command command, String label,
			List<String> arguments) {
		if (arguments.size() == 0) {
			tryCreateOwnShop((Player) sender);
		} else {
			tryCreateShopForOtherPlayer((Player) sender, arguments.get(0));
		}
	}

	private void tryCreateOwnShop(Player player) {
		if (getShopsManager().canHaveMoreShops(player)) {
			int shopPrice = getShopsManager().getShopPrice(player);
			if (getEconomy().has(player.getName(), shopPrice)) {
				getPlugin().getTemporaryValues().put(player.getName(),
						getACommand(), shopPrice);
				tell(player, ASMessage.SELECT_CHEST);
			} else {
				tell(player, ASMessage.NO_MONEY, shopPrice);
			}
		} else {
			tell(player, ASMessage.LIMIT);
		}
	}

	private void tryCreateShopForOtherPlayer(Player player, String ownerName) {
		if (getPermissions().has(player, ASPermission.OTHERS_BUY_SHOP)
				|| getPermissions().has(player, ASPermission.OTHERS_SELL_SHOP)) {
			if (PlayerUtil.isValidPlayerName(ownerName)) {
				getPlugin().getTemporaryValues().put(player.getName(),
						getACommand(), ownerName);
				tell(player, ASMessage.SELECT_CHEST);
			} else {
				tell(player, ASMessage.INVALID_PLAYER);
			}
		} else {
			tell(player, ASMessage.NO_PERMISSION);
		}
	}

}
