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
package pl.austindev.ashops.keys;

import pl.austindev.mc.APermission;

public enum ASPermission implements APermission {
	OPERATOR("operator"),
	NO_LIMIT("nolimit", OPERATOR),
	FREE("free", OPERATOR),
	ANY_PRICE_SELL("anyprice", OPERATOR),
	ANY_ITEM_SELL("anyitem", OPERATOR),
	NO_TAXES("notaxes", OPERATOR),
	ANY_REGION("anyregion", OPERATOR),
	SERVER_SELL_SHOP("server.sell", OPERATOR),
	SERVER_BUY_SHOP("server.buy", OPERATOR),
	OTHERS_SELL_SHOP("others.sell", OPERATOR, SERVER_SELL_SHOP),
	OTHERS_BUY_SHOP("others.buy", OPERATOR, SERVER_BUY_SHOP),
	PLAYER("player", OPERATOR),
	OWN_SELL_SHOP("own.sell", PLAYER, OTHERS_SELL_SHOP),
	OWN_BUY_SHOP("own.buy", PLAYER, OTHERS_BUY_SHOP),
	SELL_TO_SHOP("client.sell", PLAYER, OWN_SELL_SHOP),
	BUY_FROM_SHOP("client.buy", PLAYER, OWN_BUY_SHOP);

	private final String path;
	private final APermission[] implicating;

	private ASPermission(String path, ASPermission... implicating) {
		this.path = "ashops." + path;
		this.implicating = implicating;
	}

	public String getPath() {
		return path;
	}

	public String toString() {
		return path;
	}

	@Override
	public APermission[] getImplicating() {
		return implicating;
	}
}
