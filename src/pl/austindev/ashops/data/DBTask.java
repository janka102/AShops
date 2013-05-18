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
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;

import pl.austindev.ashops.AShops;

public abstract class DBTask implements Runnable {
	private volatile MySQLDataManager manager;

	public void perform(AShops plugin, final MySQLDataManager manager) {
		this.manager = manager;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, this);
	}

	@Override
	public void run() {
		try {
			Connection connection = manager.getConnection();
			try {
				Statement statement = connection.createStatement();
				try {
					DBTask.this.run(statement);
				} finally {
					statement.close();
				}
			} finally {
				connection.close();
			}
		} catch (SQLException e) {
			new DataAccessException("Database connection error.", e).asString();
		} catch (DataAccessException e) {
			e.asString();
		}
	}

	protected abstract void run(Statement statement) throws SQLException,
			DataAccessException;
}
