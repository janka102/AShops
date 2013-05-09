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

import org.bukkit.Location;

public class OfferLoadingException extends Exception {
	private static final long serialVersionUID = 1L;
	private final transient Location shopLocation;

	public OfferLoadingException(Throwable throwable) {
		super(throwable);
		this.shopLocation = null;
	}

	public OfferLoadingException(Throwable throwable, Location shopLocation) {
		super(throwable);
		this.shopLocation = shopLocation;
	}

	@Override
	public String getMessage() {
		return "Could not read offers from a shop. Shop location: "
				+ (shopLocation != null ? (shopLocation.getX() + "/"
						+ shopLocation.getY() + "/" + shopLocation.getZ() + "/"
						+ shopLocation.getWorld() + ".") : "unknown.");
	}
}
