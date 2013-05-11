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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.bukkit.Bukkit;

import pl.austindev.ashops.data.ContainerType;
import pl.austindev.ashops.data.DataManager;
import pl.austindev.ashops.keys.ASCommand;
import pl.austindev.ashops.keys.ASConfigurationPath;
import pl.austindev.ashops.listeners.ASBlockListener;
import pl.austindev.ashops.listeners.ASInventoryListener;
import pl.austindev.ashops.listeners.ASPlayerListener;
import pl.austindev.ashops.listeners.ASPluginListener;
import pl.austindev.mc.ACommandExecutor;
import pl.austindev.mc.APlugin;
import pl.austindev.mc.PluginSetupException;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class AShops extends APlugin {
	private volatile ShopsManager shopsManager;
	private volatile DataManager dataManager;
	private volatile WorldGuardPlugin worldGuard;
	private volatile WorldEditPlugin worldEdit;

	@Override
	public void onEnable() {
		try {
			setupConfiguration();
			setupTranslations(
					"pl.austindev.ashops.lang/ashops",
					new Locale(getConfiguration().getString(
							ASConfigurationPath.LANGUAGE)), false);
			setupPermissions();
			setupEconomy();
			setupRegionPlugins();
			setupDataManager();
			shopsManager = new ShopsManager(this);
			ACommandExecutor.register(this, ASCommand.class);
			registerListeners();
			setupTemporaryValues();
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
		}
		setupMetrics();
	}

	public ShopsManager getShopsManager() {
		return shopsManager;
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}

	public WorldGuardPlugin getWorldGuard() {
		return worldGuard;
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new ASPluginListener(this) {
				}, this);
		getServer().getPluginManager().registerEvents(
				new ASBlockListener(this) {
				}, this);
		getServer().getPluginManager().registerEvents(
				new ASInventoryListener(this) {
				}, this);
		getServer().getPluginManager().registerEvents(
				new ASPlayerListener(this) {
				}, this);
	}

	private void setupDataManager() {
		ContainerType type;
		try {
			type = ContainerType.valueOf(getConfiguration().getString(
					ASConfigurationPath.DATA_CONTAINER).toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new PluginSetupException(
					"Wrong data container type. Check the config file.");
		}
		try {
			dataManager = type.getDataManager().getConstructor(AShops.class)
					.newInstance(this);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupMetrics() {
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Don't send data.
		}
	}

	public void setupRegionPlugins() {
		try {
			this.worldEdit = (WorldEditPlugin) Bukkit.getPluginManager()
					.getPlugin("WorldEdit");
			this.worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager()
					.getPlugin("WorldGuard");
		} catch (Exception e) {
			new PluginSetupException(
					"Could not setup WorldEdit/WorldGuard. You won't be able to use some funtions in AShops.",
					e);
		}
	}
}
