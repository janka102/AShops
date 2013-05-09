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

import org.bukkit.inventory.ItemStack;

public class OfferBuilder {
	private static final int MAX_SLOT_AMOUNT = 256;

	private final ItemStack item;
	private final double price;

	private int slot;
	private int maxAmount;
	private String ownerName;

	public OfferBuilder(ItemStack item, double price) {
		this.item = item;
		this.price = price;
	}

	public Offer build(OfferType offerType) {
		if (ownerName != null) {
			if (offerType.equals(OfferType.BUY) && maxAmount > 0) {
				return new PlayerShopBuyOffer(item, price, slot, 0, Math.min(
						maxAmount, MAX_SLOT_AMOUNT), ownerName);
			} else if (offerType.equals(OfferType.SELL)) {
				return new PlayerShopSellOffer(item, price, slot, 0,
						MAX_SLOT_AMOUNT, ownerName);
			}
		} else {
			if (offerType.equals(OfferType.BUY)) {
				return new ServerShopBuyOffer(item, price, slot);
			} else if (offerType.equals(OfferType.SELL)) {
				return new ServerShopSellOffer(item, price, slot);
			}
		}
		throw new IllegalStateException();
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public void setMaxAmount(int maxAmount) {
		this.maxAmount = maxAmount;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
}
