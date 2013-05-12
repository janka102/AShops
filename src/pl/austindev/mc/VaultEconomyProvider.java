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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import pl.austindev.ashops.keys.ASConfigurationPath;

public class VaultEconomyProvider implements EconomyProvider {
	private final Economy economy;
	private boolean useID;
	private int currencyID;

	public VaultEconomyProvider(APlugin plugin) {
		RegisteredServiceProvider<Economy> rsp = plugin.getServer()
				.getServicesManager().getRegistration(Economy.class);

		useID = plugin.getConfiguration().getBoolean(
				ASConfigurationPath.USE_ID);
		currencyID = plugin.getConfiguration().getInt(
				ASConfigurationPath.CURRENCY_ID);
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
		if (useID) {
			return true;
		}
		return economy.hasAccount(playerName);
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		if (useID) {
			return true;
		}
		return economy.createPlayerAccount(playerName);
	}

	@Override
	public boolean takeFrom(Player player, double amount) {
		if (useID) {
			int removed = ItemUtil.remove(player.getInventory(), new ItemStack(currencyID), (int) amount);
			if (removed != (int) amount) {
				ItemUtil.add(player.getInventory(), new ItemStack(currencyID), removed);
				return false;
			}
			return true;
		}
		return economy.withdrawPlayer(player.getName(), amount).transactionSuccess();
	}

	@Override
	public boolean giveTo(Player player, double amount) {
		if (useID) {
			int added = ItemUtil.add(player.getInventory(), new ItemStack(currencyID), (int) amount);
			if (added != (int) amount) {
				ItemUtil.remove(player.getInventory(), new ItemStack(currencyID), added);
				return false;
			}
			return true;
		}
		return economy.depositPlayer(player.getName(), amount).transactionSuccess();
	}

	@Override
	public boolean has(Player player, double amount) {
		if (useID) {
			return ItemUtil.has(player.getInventory(), new ItemStack(currencyID), (int) amount);
		}
		return economy.has(player.getName(), amount);
	}

	@Override
	public boolean transfer(Player from, String to, double amount) {
		if (useID) {
			Player Pto = Bukkit.getPlayer(to);
			if (Pto != null) {
				if (ItemUtil.remove(from.getInventory(), new ItemStack(currencyID), (int) amount) == (int) amount)
					if (ItemUtil.add(Pto.getInventory(), new ItemStack(currencyID), (int) amount) == (int) amount)
						return true;
					else
						ItemUtil.add(from.getInventory(), new ItemStack(currencyID), (int) amount);
				return false;
			} else {
				ImprovedOfflinePlayer offlineTo = new ImprovedOfflinePlayer(to);
				if(offlineTo.exists()) {
					if (ItemUtil.remove(from.getInventory(), new ItemStack(currencyID), (int) amount) == (int) amount)
						if (ItemUtil.add(offlineTo.getInventory(), new ItemStack(currencyID), (int) amount, offlineTo) == (int) amount)
							return true;
						else
							ItemUtil.add(from.getInventory(), new ItemStack(currencyID), (int) amount);
					return false;
				}
			}
			return false;
		}
		if (economy.withdrawPlayer(from.getName(), amount).transactionSuccess())
			if (economy.depositPlayer(to, amount).transactionSuccess())
				return true;
			else
				economy.depositPlayer(from.getName(), amount);
		return false;
	}
	
	@Override
	public boolean transfer(String from, Player to, double amount) {
		if (useID) {
			Player Pfrom = Bukkit.getPlayer(from);
			if (Pfrom != null) {
				if (ItemUtil.remove(to.getInventory(), new ItemStack(currencyID), (int) amount) == (int) amount)
					if (ItemUtil.add(Pfrom.getInventory(), new ItemStack(currencyID), (int) amount) == (int) amount)
						return true;
					else
						ItemUtil.add(to.getInventory(), new ItemStack(currencyID), (int) amount);
				return false;
			} else {
				ImprovedOfflinePlayer offlineFrom = new ImprovedOfflinePlayer(to);
				if(offlineFrom.exists()) {
					if (ItemUtil.remove(to.getInventory(), new ItemStack(currencyID), (int) amount) == (int) amount)
						if (ItemUtil.add(offlineFrom.getInventory(), new ItemStack(currencyID), (int) amount, offlineFrom) == (int) amount)
							return true;
						else
							ItemUtil.add(to.getInventory(), new ItemStack(currencyID), (int) amount);
					return false;
				}
			}
			return false;
		}
		
		if (economy.withdrawPlayer(from, amount).transactionSuccess())
			if (economy.depositPlayer(to.getName(), amount).transactionSuccess())
				return true;
			else
				economy.depositPlayer(from, amount);
		return false;
	}
}
