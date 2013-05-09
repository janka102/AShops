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
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;

public class Owner implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String name;
	private volatile transient ConcurrentMap<Location, Shop> shops = new ConcurrentHashMap<Location, Shop>();

	public Owner(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addShop(Location location) {
		shops.put(location, new Shop(location));
	}

	public void removeShop(Location location) {
		shops.remove(location);
	}

	public Map<Location, Shop> getShops() {
		return shops;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(new HashSet<Shop>(shops.values()));
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		this.shops = new ConcurrentHashMap<Location, Shop>();
		for (Shop shop : ((HashSet<Shop>) in.readObject()))
			shops.put(shop.getLocation(), shop);
	}
}
