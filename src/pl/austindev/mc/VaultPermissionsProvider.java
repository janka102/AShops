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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPermissionsProvider implements PermissionsProvider {
	private final Permission permissions;

	public VaultPermissionsProvider(APlugin plugin) {
		RegisteredServiceProvider<Permission> rsp = plugin.getServer()
				.getServicesManager().getRegistration(Permission.class);
		if (rsp != null) {
			permissions = rsp.getProvider();
			if (permissions == null) {
				throw new PluginSetupException(
						"Could not find any permissions plugin.");
			}
		} else {
			throw new PluginSetupException(
					"Could not find any permissions plugin.");
		}
	}

	@Override
	public Set<String> getGroups(Player player) {
		return new HashSet<String>(Arrays.asList(permissions
				.getPlayerGroups(player)));
	}

	@Override
	public Set<String> getGroups(String playerName, World world) {
		return new HashSet<String>(Arrays.asList(permissions.getPlayerGroups(
				world, playerName)));
	}

	@Override
	public boolean has(CommandSender player, APermission permission) {
		if (player.hasPermission(permission.getPath())) {
			return true;
		}
		for (APermission p : permission.getImplicating()) {
			if (has(player, p)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean has(String playerName, World world, APermission permission) {
		if (permissions.has(world, playerName, permission.getPath())) {
			return true;
		} else {
			for (APermission p : permission.getImplicating()) {
				if (has(playerName, world, p)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public boolean hasOneOf(CommandSender player, APermission... permissions) {
		for (APermission permission : permissions)
			if (has(player, permission))
				return true;
		return false;
	}

	@Override
	public boolean hasOneOf(CommandSender player, APermission permission1,
			APermission permission2) {
		return has(player, permission1) || has(player, permission2);
	}

	@Override
	public boolean hasAll(CommandSender player, APermission... permissions) {
		for (APermission permission : permissions)
			if (!has(player, permission))
				return false;
		return true;
	}

	@Override
	public boolean hasAll(CommandSender player, APermission permission1,
			APermission permission2) {
		return has(player, permission1) && has(player, permission2);
	}

	@Override
	public String[] getGroups() {
		return permissions.getGroups();
	}
}
