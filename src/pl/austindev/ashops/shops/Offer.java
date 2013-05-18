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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.keys.ASMessage;
import pl.austindev.ashops.keys.ASPermission;
import pl.austindev.mc.PlayerUtil;
import pl.austindev.mc.SerializationUtils;

public abstract class Offer implements Serializable {
	private static final long serialVersionUID = 1L;
	private volatile transient ItemStack item;
	private final double price;
	private volatile int slot;
	private volatile boolean modified;

	protected Offer(ItemStack item, double price, int slot) {
		this.item = item;
		this.price = price;
		this.slot = slot;
	}

	public static Offer getOffer(ItemStack offerTag, int slot) {
		if (offerTag.hasItemMeta()) {
			List<String> lore = offerTag.getItemMeta().getLore();
			if (PlayerUtil.isValidPlayerName(lore.get(lore.size() - 1)
					.substring(2))) {
				return PlayerShopOffer.getOffer(offerTag, slot);
			} else {
				return ServerShopOffer.getOffer(offerTag, slot);
			}
		} else {
			return null;
		}
	}

	public ItemStack getItem() {
		return item;
	}

	public double getPrice() {
		return price;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public abstract ASMessage trade(AShops plugin, Player player,
			Inventory shopInventory, int amount);

	public abstract void updateOfferTag(Inventory inventory);

	public abstract ASPermission getPermission();

	public boolean isModified() {
		return modified;
	}

	protected void setModified() {
		this.modified = true;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(SerializationUtils.serialize(item));
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.item = (ItemStack) SerializationUtils
				.deserialize((Map<String, Object>) in.readObject());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Offer))
			return false;
		Offer offer = (Offer) obj;
		return this.slot == offer.slot;
	}

	@Override
	public int hashCode() {
		return ((Integer) this.slot).hashCode();
	}
}