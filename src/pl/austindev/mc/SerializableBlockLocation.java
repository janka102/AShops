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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializableBlockLocation implements Serializable {
	private static final long serialVersionUID = 1L;
	private final String worldName;
	private final int x;
	private final int y;
	private final int z;

	public SerializableBlockLocation(String worldName, int x, int y, int z) {
		this.worldName = worldName;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public SerializableBlockLocation(Location location) {
		this(location.getWorld().getName(), location.getBlockX(), location
				.getBlockY(), location.getBlockZ());
	}

	public Location toLocation() {
		return new Location(Bukkit.getWorld(worldName), x, y, z);
	}

	public static Set<SerializableBlockLocation> serializeSet(Set<Location> set) {
		Set<SerializableBlockLocation> serializableSet = new HashSet<SerializableBlockLocation>();
		for (Location location : new HashSet<Location>(set))
			serializableSet.add(new SerializableBlockLocation(location));
		return serializableSet;
	}

	public static Set<Location> unserializeSet(
			Set<SerializableBlockLocation> serializableSet) {
		Set<Location> set = new HashSet<Location>();
		for (SerializableBlockLocation serializableLocation : serializableSet)
			set.add(serializableLocation.toLocation());
		return set;
	}
}
