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

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class AListener<T extends APlugin> implements Listener {
	private final T plugin;

	public AListener(T plugin) {
		this.plugin = plugin;
	}

	public void register() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public T getPlugin() {
		return plugin;
	}

	public PermissionsProvider getPermissions() {
		return plugin.getPermissions();
	}

	public EconomyProvider getEconomy() {
		return plugin.getEconomy();
	}

	public void tell(Player player, AMessage key, Object... arguments) {
		player.sendMessage(getPlugin().$(key, arguments));
	}
}
