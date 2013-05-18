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
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.InventoryUtils;
import pl.austindev.ashops.keys.ASCommand;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.ashops.shops.OfferBuilder;

public class ABuyCommandExecutor extends ASCommandExecutor {

	public ABuyCommandExecutor(AShops plugin) {
		super(plugin, ASCommand.ABUY);
	}

	@Override
	protected void run(CommandSender sender, Command command, String label,
			List<String> arguments) {
		Player player = (Player) sender;
		double price = InventoryUtils.getPrice(arguments.get(0));
		if (price > 0) {
			int amount = InventoryUtils.getAmount(arguments.get(1));
			if (amount > 0) {
				ItemStack item = null;
				if (arguments.size() > 2)
					item = InventoryUtils.getItem(arguments.get(2));
				else if (player.getItemInHand() != null)
					item = InventoryUtils.getItem(player.getItemInHand());
				if (item != null && item.getTypeId() > 0) {
					if (getShopsManager().canTrade(player, item)) {
						double minimalPrice = getShopsManager()
								.getMinimalPrice(player, item);
						if (price >= minimalPrice) {
							OfferBuilder offerBuilder = new OfferBuilder(item,
									price);
							offerBuilder.setMaxAmount(amount);
							getPlugin().getTemporaryValues().put(
									player.getName(), getACommand(),
									offerBuilder);
							tell(player, ASMessage.SELECT_CHEST);
						} else {
							tell(player, ASMessage.MINIMAL_PRICE, minimalPrice);
						}
					} else {
						tell(player, ASMessage.ITEM_EXCLUDED);
					}
				} else {
					tell(player, ASMessage.WRONG_ITEM);
				}
			} else {
				tell(player, ASMessage.WRONG_AMOUNT);
			}
		} else {
			tell(player, ASMessage.WRONG_PRICE);
		}
	}
}