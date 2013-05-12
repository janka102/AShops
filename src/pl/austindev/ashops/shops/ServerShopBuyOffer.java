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
package pl.austindev.ashops.shops;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.ShopUtils;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.mc.ItemUtil;

public class ServerShopBuyOffer extends ServerShopOffer {
	private static final long serialVersionUID = 1L;

	public ServerShopBuyOffer(ItemStack item, double price, int slot) {
		super(item, price, slot);
	}

	@Override
	public void updateOfferTag(Inventory inventory) {
		setModified();
		ItemStack offerTag = new ItemStack(getItem());
		ItemMeta meta = offerTag.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null)
			lore = new LinkedList<String>();
		lore.add(ChatColor.GREEN + "+"
				+ String.format(Locale.ENGLISH, "%.2f", getPrice()));
		meta.setLore(lore);
		offerTag.setItemMeta(meta);
		inventory.setItem(getSlot(), offerTag);
	}

	@Override
	public synchronized ASMessage trade(AShops plugin, Player player,
			Inventory shopInventory, int amount) {
		Inventory playerInventory = player.getInventory();
		int playerSlot = ItemUtil.firstSimilar(playerInventory, getItem());
		if (playerSlot > -1) {
			amount = Math.min(amount, getItem().getMaxStackSize());
			amount = ItemUtil.remove(playerInventory, getItem(), amount);
			double value = getPrice() * amount;
			String accountName = ShopUtils.getServerAccountName();
			if (accountName != null) {
				if (plugin.getEconomy().transfer(accountName, player,
						value)) {
					updateOfferTag(shopInventory);
					ShopUtils.applyTaxes(plugin, player, value);
					return null;
				} else {
					ItemUtil.add(playerInventory, getItem(), amount);
					return ASMessage.OWNER_NO_MONEY;
				}
			} else {
				if (plugin.getEconomy().giveTo(player, value)) {
					ShopUtils.applyTaxes(plugin, player, value);
					return null;
				} else {
					ItemUtil.add(playerInventory, getItem(), amount);
					return ASMessage.OWNER_NO_MONEY;
				}
			}
		} else {
			return ASMessage.CLIENT_NO_ITEM;
		}
	}

	@Override
	public ASPermission getPermission() {
		return ASPermission.SELL_TO_SHOP;
	}
}
