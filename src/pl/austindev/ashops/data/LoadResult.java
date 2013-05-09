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

import java.util.Set;

import pl.austindev.ashops.shops.Owner;
import pl.austindev.ashops.shops.Shop;

public class LoadResult {
	private final Set<Owner> loadedOwners;
	private final Set<Shop> loadedServerShops;

	public LoadResult(Set<Owner> loadedOwners, Set<Shop> loadedServerShops) {
		this.loadedOwners = loadedOwners;
		this.loadedServerShops = loadedServerShops;
	}

	public Set<Owner> getLoadedOwners() {
		return loadedOwners;
	}

	public Set<Shop> getLoadedServerShops() {
		return loadedServerShops;
	}
}
