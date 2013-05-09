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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;

import pl.austindev.ashops.AShops;
import pl.austindev.ashops.keys.ASConfigurationPath;

public class SimpleMySQLManager {
	private final AShops plugin;

	private final String host;
	private final String name;
	private final String user;
	private final String password;

	public SimpleMySQLManager(AShops plugin) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		this.plugin = plugin;
		this.host = plugin.getConfiguration().getString(
				ASConfigurationPath.DB_HOST);
		this.name = plugin.getConfiguration().getString(
				ASConfigurationPath.DB_NAME);
		this.user = plugin.getConfiguration().getString(
				ASConfigurationPath.DB_USER);
		this.password = plugin.getConfiguration().getString(
				ASConfigurationPath.DB_PASSWORD);
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + host + "/" + name
				+ "?" + "user=" + user + "&password=" + password);
	}

	public void scheduleQuery(final String query) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					try {
						Statement statement = connection.createStatement();
						try {
							statement.executeUpdate(query);
						} finally {
							statement.close();
						}
					} finally {
						connection.close();
					}
				} catch (SQLException e) {
					new DataAccessException(
							"Could not connect to a database. Check the plugin's config file.",
							e).printStackTrace();
				}
			}
		});
	}

	public void scheduleQuery(final String... queries) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					Connection connection = getConnection();
					try {
						Statement statement = connection.createStatement();
						try {
							for (String query : queries)
								statement.executeUpdate(query);
						} finally {
							statement.close();
						}
					} finally {
						connection.close();
					}
				} catch (SQLException e) {
					new DataAccessException(
							"Could not connect to a database. Check the plugin's config file.",
							e).printStackTrace();
				}
			}
		});
	}

}
