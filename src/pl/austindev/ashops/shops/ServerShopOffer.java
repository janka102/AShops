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

import java.util.List;

import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.InventoryUtils;
import pl.austindev.ashops.ShopUtils;

public abstract class ServerShopOffer extends Offer {
	private static final long serialVersionUID = 1L;

	protected ServerShopOffer(ItemStack item, double price, int slot) {
		super(item, price, slot);
	}

	public static Offer getOffer(ItemStack offerTag, int slot) {
		try {
			if (offerTag.hasItemMeta()) {
				List<String> lore = offerTag.getItemMeta().getLore();
				if (lore.size() > 0) {
					String priceLine = lore.get(lore.size() - 1);
					double price = ShopUtils.extractPrice(priceLine);
					ItemStack rawItem = InventoryUtils.getReducedItem(offerTag,
							1);
					return price > 0 ? new ServerShopBuyOffer(rawItem, price,
							slot) : new ServerShopSellOffer(rawItem,
							Math.abs(price), slot);
				}
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return null;
	}
}
