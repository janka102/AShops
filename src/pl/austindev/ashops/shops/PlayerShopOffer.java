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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.InventoryUtils;
import pl.austindev.ashops.keys.ASMessage;

public abstract class PlayerShopOffer extends Offer {
	private static final long serialVersionUID = 1L;
	private final String ownerName;
	private volatile int amount;
	private final int maxAmount;

	protected PlayerShopOffer(ItemStack item, double price, int slot,
			int amount, int maxAmount, String ownerName) {
		super(item, price, slot);
		this.ownerName = ownerName;
		this.amount = amount;
		this.maxAmount = maxAmount;
	}

	public static Offer getOffer(ItemStack offerTag, int slot) {
		List<String> lore = offerTag.getItemMeta().getLore();
		String ownerName = lore.get(lore.size() - 1).substring(2);
		String[] amounts = lore.get(lore.size() - 2).split("/");
		int amount = Integer.parseInt(amounts[0].substring(2));
		int maxAmount = Integer.parseInt(amounts[1]);
		double price = Double.parseDouble(lore.get(lore.size() - 3)
				.substring(2));
		ItemStack rawItem = InventoryUtils.getReducedItem(offerTag, 3);
		return price > 0 ? new PlayerShopBuyOffer(rawItem, price, slot, amount,
				maxAmount, ownerName) : new PlayerShopSellOffer(rawItem,
				Math.abs(price), slot, amount, maxAmount, ownerName);
	}

	public synchronized Set<ItemStack> takeContents() {
		Set<ItemStack> stacks = new HashSet<ItemStack>();
		int maxStackSize = getItem().getMaxStackSize();
		for (int currentAmount = getAmount() % maxStackSize; getAmount() > 0; setAmount(getAmount()
				- currentAmount)) {
			ItemStack item = new ItemStack(getItem());
			item.setAmount(currentAmount);
			stacks.add(item);
		}
		return stacks;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	@SuppressWarnings("deprecation")
	public synchronized ASMessage collect(Player player, Inventory shopInventory) {
		Inventory playerInventory = player.getInventory();
		int playerSlot = playerInventory.firstEmpty();
		if (playerSlot > -1) {
			int amount = Math.min(getAmount(), getItem().getMaxStackSize());
			if (amount > 0) {
				ItemStack items = new ItemStack(getItem());
				items.setAmount(amount);
				playerInventory.setItem(playerSlot, items);
				setAmount(getAmount() - amount);
				updateOfferTag(shopInventory);
			} else {
				return ASMessage.NO_ITEMS_TO_COLLECT;
			}
		} else {
			return ASMessage.NO_SPACE_FOR_ITEMS;
		}
		player.updateInventory();
		return null;
	}
}
