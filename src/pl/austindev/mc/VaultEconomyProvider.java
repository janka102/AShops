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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomyProvider implements EconomyProvider {
	private final Economy economy;

	public VaultEconomyProvider(APlugin plugin) {
		RegisteredServiceProvider<Economy> rsp = plugin.getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (rsp != null) {
			economy = rsp.getProvider();
			if (economy == null) {
				throw new PluginSetupException(
						"Could not find any permissions plugin.");
			}
		} else {
			throw new PluginSetupException("Could not find any economy plugin.");
		}
	}

	@Override
	public boolean hasAccount(String playerName) {
		return economy.hasAccount(playerName);
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		return economy.createPlayerAccount(playerName);
	}

	@Override
	public boolean takeFrom(String playerName, double amount) {
		return economy.withdrawPlayer(playerName, amount).transactionSuccess();
	}

	@Override
	public boolean giveTo(String playerName, double amount) {
		return economy.depositPlayer(playerName, amount).transactionSuccess();
	}

	@Override
	public boolean has(String playerName, double amount) {
		return economy.has(playerName, amount);
	}

	@Override
	public boolean transfer(String from, String to, double amount) {
		if (economy.withdrawPlayer(from, amount).transactionSuccess())
			if (economy.depositPlayer(to, amount).transactionSuccess())
				return true;
			else
				economy.depositPlayer(from, amount);
		return false;
	}
}
