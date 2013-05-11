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

import pl.austindev.mc.ConfigurationPath;

public enum ASConfigurationPath implements ConfigurationPath {
	DEBUG("debug"), LANGUAGE("language"), DATA_CONTAINER("data_container"), DB_HOST(
			"host"), DB_NAME("name"), DB_USER("user"), DB_PASSWORD("password"), SHOPS_LIMIT(
			"limit.%s"), SHOP_PRICE("price.%s"), MINIMAL_PRICE("minimal.%s"), EXCLUDED_ITEMS_LIST(
			"exclude"), DISCOUNT("discount.%s"), SERVER_ACCOUNT_NAME(
			"server_account_name"), TAXES_ACCOUNT_NAME("taxes_account_name"), TAXES(
			"taxes.%s");
	private final String path;

	private ASConfigurationPath(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}
}
