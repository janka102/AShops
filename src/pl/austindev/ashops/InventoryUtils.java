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
package pl.austindev.ashops;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryUtils {
	public static ItemStack getReducedItem(ItemStack offerTag, int toReduce) {
		ItemStack item = new ItemStack(offerTag);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore = lore.subList(0, lore.size() - toReduce);
		if (lore.size() == 0)
			lore = null;
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static int getClickedAmount(InventoryClickEvent event) {
		int amount;
		if (event.isShiftClick()) {
			amount = 64;
		} else {
			amount = event.isLeftClick() ? 1 : 16;
		}
		return amount;
	}

	public static double getPrice(String string) {
		double price = 0;
		try {
			price = Double.parseDouble(string);
		} catch (NumberFormatException e) {
			// Price stays 0.
		}
		return price;
	}

	public static int getAmount(String string) {
		int amount = 0;
		try {
			amount = Integer.parseInt(string);
		} catch (NumberFormatException e) {
			// Amount stays 0.
		}
		return amount;
	}

	public static ItemStack getItem(String string) {
		String[] itemCode = string.split(":");
		if (itemCode.length > 0) {
			int id = extractId(itemCode[0]);
			if (id > 0) {
				if (itemCode.length > 1) {
					short data = extractData(itemCode[1]);
					if (data >= 0) {
						if (itemCode.length > 2) {
							Map<Enchantment, Integer> enchants = extractEnchantments(itemCode[2]);
							if (enchants != null) {
								return getItem(id, data, enchants);
							}
						} else
							return getItem(id, data, null);
					}
				} else
					return getItem(id, (short) 0, null);
			}
		}
		return null;
	}

	private static int extractId(String string) {
		Material material;
		if (string.matches("[0-9]+")) {
			int id = Integer.parseInt(string);
			material = Material.getMaterial(id);
		} else {
			material = Material.getMaterial(string.toUpperCase());
		}
		return material != null ? material.getId() : -1;
	}

	private static short extractData(String string) {
		try {
			return Short.parseShort(string);
		} catch (NumberFormatException e) {
		}
		return -1;
	}

	private static Map<Enchantment, Integer> extractEnchantments(String string) {
		Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		for (String enchantCode : string.split(",")) {
			String[] enchant = enchantCode.split("-");
			if (enchant.length == 2) {
				Enchantment ench = extractEnchant(enchant[0]);
				if (ench != null) {
					int level = extractEnchantLevel(enchant[1]);
					if (level > 0) {
						if (level <= ench.getMaxLevel()) {
							enchants.put(ench, level);
						} else
							return null;
					} else
						return null;
				} else
					return null;
			} else
				return null;
		}
		return enchants;
	}

	private static Enchantment extractEnchant(String string) {
		Enchantment enchantment;
		if (string.matches("[0-9]+")) {
			int id = Integer.parseInt(string);
			enchantment = Enchantment.getById(id);
		} else {
			enchantment = Enchantment.getByName(string);
		}
		return enchantment;
	}

	private static int extractEnchantLevel(String string) {
		return string.matches("[0-9]+") ? Integer.parseInt(string) : -1;
	}

	private static ItemStack getItem(int id, short data,
			Map<Enchantment, Integer> enchantments) {
		ItemStack item = new ItemStack(id, 1, data);
		if (enchantments != null)
			for (Entry<Enchantment, Integer> e : enchantments.entrySet())
				if (e.getKey().canEnchantItem(item))
					item.addEnchantment(e.getKey(), e.getValue());
				else
					return null;
		return item;
	}

	public static ItemStack getItem(ItemStack item) {
		ItemStack newItem = new ItemStack(item);
		newItem.setAmount(1);
		return newItem;
	}
}
