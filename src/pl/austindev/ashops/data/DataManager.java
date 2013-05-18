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
package pl.austindev.ashops.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.bukkit.Location;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.shops.Offer;
import pl.austindev.ashops.shops.Owner;
import pl.austindev.ashops.shops.Shop;

public abstract class DataManager {
	private final AShops plugin;

	protected DataManager(AShops plugin) {
		this.plugin = plugin;
	}

	public AShops getPlugin() {
		return plugin;
	}

	public void saveToFile() throws DataAccessException {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(getShopsFile()));
			try {
				out.writeObject(getOwners());
				out.writeObject(getServerShops());
			} finally {
				out.close();
			}
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
	}

	public abstract LoadResult start() throws DataAccessException;

	public abstract int countShops(String ownerName);

	public abstract void addPlayerShop(Location location, String ownerName);

	public abstract void addServerShop(Location location);

	public abstract void removePlayerShop(Location location, String ownerName);

	public abstract void removeServerShop(Location location);

	public abstract void addOffer(Location location, Offer offer);

	public abstract void removeOffer(Location location, Offer offer);

	public abstract void updateOffers(Shop shop);

	public abstract Set<Owner> getOwners() throws DataAccessException;

	public abstract Set<Shop> getServerShops() throws DataAccessException;

	public abstract void close();

	public abstract void clearPlayerShops(String ownerName)
			throws DataAccessException;

	public abstract void clearPlayerShops() throws DataAccessException;

	public abstract void clearServerShops() throws DataAccessException;

	public abstract LoadResult loadFromFile() throws DataAccessException;

	protected File getShopsFile() {
		return new File(plugin.getDataFolder(), "shops.dat");
	}

	public abstract Owner getOwner(String ownerName) throws DataAccessException;

	public abstract void addOffers(Location location, Set<Offer> offers);

	public abstract void synchUpdateOffers(Shop shop)
			throws DataAccessException;
}