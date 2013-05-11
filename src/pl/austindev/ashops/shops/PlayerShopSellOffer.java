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

public class PlayerShopSellOffer extends PlayerShopOffer {
	private static final long serialVersionUID = 1L;

	public PlayerShopSellOffer(ItemStack item, double price, int slot,
			int amount, int maxAmount, String ownerName) {
		super(item, price, slot, amount, maxAmount, ownerName);
	}

	@Override
	public void updateOfferTag(Inventory inventory) {
		setModified();
		ItemStack offerTag = new ItemStack(getItem());
		ItemMeta meta = offerTag.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null)
			lore = new LinkedList<String>();
		lore.add(ChatColor.RED + "-"
				+ String.format(Locale.ENGLISH, "%.2f", getPrice()));
		lore.add("" + ChatColor.GRAY + getAmount() + "/" + getMaxAmount());
		lore.add(ChatColor.GRAY + getOwnerName());
		meta.setLore(lore);
		offerTag.setItemMeta(meta);
		inventory.setItem(getSlot(), offerTag);
	}

	@SuppressWarnings("deprecation")
	public synchronized ASMessage load(Player player, Inventory shopInventory) {
		Inventory playerInventory = player.getInventory();
		int playerSlot = ItemUtil.firstSimilar(playerInventory, getItem());
		if (playerSlot > -1) {
			ItemStack playerItem = playerInventory.getItem(playerSlot);
			int amount = Math.min(getMaxAmount() - getAmount(),
					playerItem.getAmount());
			if (amount > 0) {
				setAmount(getAmount() + amount);
				playerItem.setAmount(playerItem.getAmount() - amount);
				playerInventory.setItem(playerSlot,
						playerItem.getAmount() > 0 ? playerItem : null);
				updateOfferTag(shopInventory);
				player.updateInventory();
			} else {
				return ASMessage.NO_SPACE_FOR_ITEMS;
			}
		} else {
			return ASMessage.NO_ITEMS_TO_ADD;
		}
		return null;
	}

	@Override
	public synchronized ASMessage trade(AShops plugin, Player player,
			Inventory shopInventory, int amount) {
		Inventory playerInventory = player.getInventory();
		int playerSlot = ItemUtil.firstSimilar(playerInventory, getItem());
		if (playerSlot < 0)
			playerSlot = playerInventory.firstEmpty();
		if (playerSlot >= 0) {
			amount = Math.min(amount, getItem().getMaxStackSize());
			amount = Math.min(amount, getAmount());
			if (amount > 0) {
				amount = ItemUtil.add(playerInventory, getItem(), amount);
				double value = getPrice() * amount;
				if (plugin.getEconomy().transfer(player.getName(),
						getOwnerName(), value)) {
					setAmount(getAmount() - amount);
					updateOfferTag(shopInventory);
					ShopUtils.applyTaxes(plugin, player.getWorld(), getOwnerName(),
							value);
					return null;
				} else {
					ItemUtil.remove(playerInventory, getItem(), amount);
					return ASMessage.CLIENT_NO_MONEY;
				}
			} else {
				return ASMessage.OWNER_NO_ITEMS;
			}
		} else {
			return ASMessage.CLIENT_NO_SPACE;
		}
	}

	@Override
	public ASPermission getPermission() {
		return ASPermission.BUY_FROM_SHOP;
	}
}
