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
package pl.austindev.ashops.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.OfferLoadingException;
import pl.austindev.ashops.data.DataAccessException;

public class ASPluginListener extends ASListener {

	public ASPluginListener(AShops plugin) {
		super(plugin);
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().equals(getPlugin())) {
			try {
				getShopsManager().close();
			} catch (OfferLoadingException e) {
				e.printStackTrace();
			} catch (DataAccessException e) {
				new RuntimeException(e);
			}
			getPlugin().getDataManager().close();
		}
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {

	}
}
