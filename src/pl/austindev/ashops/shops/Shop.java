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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;

import pl.austindev.mc.SerializableBlockLocation;

public class Shop implements Serializable {
	private static final long serialVersionUID = 1L;
	private volatile transient Location location;
	private final ConcurrentMap<Integer, Offer> offers = new ConcurrentHashMap<Integer, Offer>();
	private final String ownerName;

	public Shop(Location location) {
		this(location, null);
	}

	public Shop(Location location, String ownerName) {
		this.location = location;
		this.ownerName = ownerName;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public Location getLocation() {
		return location;
	}

	public void addOffer(int slot, Offer offer) {
		offers.put(slot, offer);
	}

	public Offer removeOffer(int slot) {
		return offers.remove(slot);
	}

	public Offer getOffer(int slot) {
		return offers.get(slot);
	}

	public ConcurrentMap<Integer, Offer> getOffers() {
		return offers;
	}

	public boolean isModified() {
		for (Map.Entry<Integer, Offer> entry : offers.entrySet())
			if (entry.getValue().isModified()) {
				return true;
			}
		return false;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(new SerializableBlockLocation(location));
	}

	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.location = ((SerializableBlockLocation) in.readObject())
				.toLocation();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Offer))
			return false;
		Shop shop = (Shop) obj;
		return location.equals(shop.getLocation());
	}

	@Override
	public int hashCode() {
		return location.hashCode();
	}
}
