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

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class ItemUtil {

	public static int lastEmpty(Inventory inventory) {
		for (int i = inventory.getSize() - 1; i >= 0; i--) {
			ItemStack item = inventory.getItem(i);
			if (item == null || item.getTypeId() < 1)
				return i;
		}
		return -1;
	}

	public static int firstSimilar(Inventory inventory, ItemStack item) {
		int first = inventory.first(item.getTypeId());
		if (first > -1)
			for (int i = first; i < inventory.getSize(); i++) {
				if (item.isSimilar(inventory.getItem(i)))
					return i;
			}
		return -1;
	}

	@SuppressWarnings("deprecation")
	public static int remove(Inventory inventory, ItemStack item,
			int requestedAmount) {
		int leftAmount = requestedAmount;
		for (int i = firstSimilar(inventory, item); i > -1 && leftAmount > 0; i = firstSimilar(
				inventory, item)) {
			ItemStack itemInSlot = inventory.getItem(i);
			int amountToRemove = Math.min(itemInSlot.getAmount(), leftAmount);
			itemInSlot.setAmount(itemInSlot.getAmount() - amountToRemove);
			inventory
					.setItem(i, itemInSlot.getAmount() > 0 ? itemInSlot : null);
			leftAmount -= amountToRemove;
		}
		if (inventory.getType().equals(InventoryType.PLAYER))
			((Player) inventory.getHolder()).updateInventory();
		return requestedAmount - leftAmount;
	}

	@SuppressWarnings("deprecation")
	public static int add(Inventory inventory, ItemStack item,
			int requestedAmount) {
		int leftAmount = requestedAmount;
		int firstEmpty = inventory.firstEmpty();
		int first = firstSimilar(inventory, item);
		if (firstEmpty > -1)
			first = first > -1 ? Math.min(first, firstEmpty) : firstEmpty;
		if (first > -1) {
			for (int i = first; i < inventory.getSize() && leftAmount > 0; i++) {
				ItemStack itemInSlot = inventory.getItem(i);
				if (itemInSlot == null || itemInSlot.getTypeId() < 1) {
					int amountToAdd = Math.min(item.getMaxStackSize(),
							leftAmount);
					ItemStack itemToAdd = new ItemStack(item);
					itemToAdd.setAmount(amountToAdd);
					inventory.setItem(i, itemToAdd);
					leftAmount -= amountToAdd;
				} else if (itemInSlot.isSimilar(item)) {
					int amountToAdd = Math.min(item.getMaxStackSize()
							- itemInSlot.getAmount(), leftAmount);
					if (amountToAdd > 0) {
						ItemStack newItem = new ItemStack(item);
						newItem.setAmount(itemInSlot.getAmount() + amountToAdd);
						inventory.setItem(i, newItem);
						leftAmount -= amountToAdd;
					}
				}
			}
		}
		if (inventory.getType().equals(InventoryType.PLAYER))
			((Player) inventory.getHolder()).updateInventory();
		return requestedAmount - leftAmount;
	}
}
