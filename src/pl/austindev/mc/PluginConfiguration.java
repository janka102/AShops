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

import java.util.List;
import java.util.Locale;

public class PluginConfiguration {
	private final APlugin plugin;

	public PluginConfiguration(APlugin pluginBase) {
		this.plugin = pluginBase;
	}

	public String getString(ConfigurationPath path, Object... arguments) {
		return plugin.getConfig().getString(getPath(path, arguments));
	}

	public int getInt(ConfigurationPath path, Object... arguments) {
		return plugin.getConfig().getInt(getPath(path, arguments));
	}

	public double getDouble(ConfigurationPath path, Object... arguments) {
		return plugin.getConfig().getDouble(getPath(path, arguments));
	}

	public boolean getBoolean(ConfigurationPath path, Object... arguments) {
		return plugin.getConfig().getBoolean(getPath(path, arguments));
	}

	public List<Integer> getIntegerList(ConfigurationPath path,
			Object... arguments) {
		return plugin.getConfig().getIntegerList(getPath(path, arguments));
	}

	private String getPath(ConfigurationPath path, Object... arguments) {
		return String.format(Locale.ENGLISH, path.getPath(), arguments);
	}
}
